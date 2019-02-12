package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by jmussler on 4/22/15.
 */
public class CheckData {
    // TODO: Move Tag filtering logic to the generic data model
    private Set<String> entityTagFields;
    // adding alias,account_alias,cluster_alias due to legacy, and should be exclusive anyways
    private final static Set<String> DEFAULT_ENTITY_TAG_FIELDS = new HashSet<>(
            ImmutableSet.of("application_id", "application_version", "stack_name", "stack_version", "application",
                    "version", "account_alias", "cluster_alias", "alias", "namespace"));
    private static final String REPLACE_CHAR = "_";
    private static final Pattern INVALID_TAG_CHARS = Pattern.compile("[?@:=\\[\\]]");


    public String time;
    public String worker;
    @JsonProperty("check_id")
    public int checkId;
    @JsonProperty("entity_id")
    public String entityId;
    public Map<String,String> entity = new HashMap<>();
    @JsonProperty("run_time")
    public double runTime;
    @JsonProperty("check_result")
    public JsonNode checkResult;
    public boolean exception;
    public Map<String, AlertData> alerts = new HashMap<>(0);
    @JsonProperty("is_sampled")
    public boolean isSampled = true;

    private static final Logger LOG = LoggerFactory.getLogger(CheckData.class);
    private static final String TIME_SERIES_PREFIX = "zmon.check.id.";

    // Generic time series data model
    public Long timeStampLong;
    public List<ZmonTimeSeriesMetrics> dataPoints;

    public static class ZmonTimeSeriesMetrics{
        public String id;
        public Long value;
        public Map<String, String> tags;
    }

    public void formatTimeSeriesMetrics(){

        // TODO: Move Tag filtering logic to the generic data model
        if (null == config.getKairosdbTagFields() || config.getKairosdbTagFields().size() == 0) {
            this.entityTagFields = DEFAULT_ENTITY_TAG_FIELDS;
        } else {
            this.entityTagFields = new HashSet<>(config.getKairosdbTagFields());
        }

        if (!this.isSampled) {
            LOG.debug("Dropping non-sampled metrics for checkid={}", this.checkId);
            return;
        }

        final double timeStamp = this.checkResult.get("ts").asDouble();
        this.timeStampLong= (long) (timeStamp * 1000L);

        String timeSeries = TIME_SERIES_PREFIX + this.checkId;

        Map<String, NumericNode> values = new HashMap<>();
        fillFlatValueMap(values, "", this.checkResult.get("value"));
        mapDataPoints(values, timeSeries);
    }

    /**
     * Flattens the JSONNode that contains a single check results into a map
     *
     * @param values
     * @param prefix
     * @param base
     */
    public void fillFlatValueMap(Map<String, NumericNode> values, String prefix, JsonNode base) {
        if (base instanceof NumericNode) {
            values.put(prefix, (NumericNode) base);
        } else if (base instanceof TextNode) {
            // try to convert string node in case it is numeric
            try {
                TextNode t = (TextNode) base;
                BigDecimal db = new BigDecimal(t.textValue());
                DecimalNode dn = new DecimalNode(db);
                values.put(prefix, dn);
            } catch (NumberFormatException ex) {
                // Ignore
            }
        } else if (base instanceof ObjectNode) {
            Iterator<String> i = base.fieldNames();
            while (i.hasNext()) {
                String k = i.next();

                if (prefix.length() == 0) {
                    fillFlatValueMap(values, k, base.get(k));
                } else {
                    fillFlatValueMap(values, prefix + "." + k, base.get(k));
                }
            }
        }
    }

    public void mapDataPoints(Map<String, NumericNode> values, String timeSeries){

        for (Map.Entry<String, NumericNode> e : values.entrySet()) {

            ZmonTimeSeriesMetrics metrics = new ZmonTimeSeriesMetrics();

            String key = e.getKey();
            String[] keyParts = key.split("\\.");
            if (keyParts.length >= 3 && "health".equals(keyParts[0]) && "200".equals(keyParts[2])) {
                // remove the 200 health check data points, with 1/sec * instances with elb checks they just confuse
                continue;
            }

            // Data points id = "zmon.check.1234.cpu_latency_p99"
            if (StringUtils.hasText(key)){
                metrics.id = timeSeries + "." + key;
            } else {
                metrics.id = timeSeries;
            }

            final Map<String, String> tags = getTags(key, this.entityId, this.entity);
            if (config.getActuatorMetricChecks().contains(this.checkId)) {
                addActuatorMetricTags(keyParts, tags);
            }

            metrics.tags = tags;
            metrics.value = e.getValue().asLong();

            this.dataPoints.add(metrics);
        }
    }

    public Map<String, String> getTags(String key, String entityId, Map<String, String> entity) {
        Map<String, String> tags = new HashMap<>();
        tags.put("entity", INVALID_TAG_CHARS.matcher(entityId).replaceAll(REPLACE_CHAR));

        for (String field : entityTagFields) {
            if (entity.containsKey(field)) {
                String fieldValue = entity.get(field);
                if (null != fieldValue && !"".equals(fieldValue)) {
                    tags.put(field, entity.get(field));
                }
            }
        }

        if (null != key && !"".equals(key)) {
            tags.put("key", INVALID_TAG_CHARS.matcher(key).replaceAll(REPLACE_CHAR));
        }

        String metricName = extractMetricName(key);
        if (null != metricName) {
            tags.put("metric", INVALID_TAG_CHARS.matcher(metricName).replaceAll(REPLACE_CHAR));
        }

        return tags;
    }

    private static String extractMetricName(String key) {
        if (null == key || "".equals(key)) return null;
        String[] keyParts = key.split("\\.");
        String metricName = keyParts[keyParts.length - 1];
        if ("".equals(metricName)) {
            metricName = keyParts[keyParts.length - 2];
        }
        return metricName;
    }

    /*
        handle zmon actuator metrics and extract the http status code into its own field
        put the first character of the status code into "status group" sg, this is only for easy kairosdb query
    */
    private void addActuatorMetricTags(String[] keyParts, Map<String, String> tags) {
        if (keyParts.length >= 3) {
            final String statusCode = keyParts[keyParts.length - 2];
            tags.put("sc", statusCode);
            tags.put("sg", statusCode.substring(0, 1));

            if (keyParts.length >= 4) {
                StringBuilder b = new StringBuilder();
                for (int i = 0; i < keyParts.length - 3; ++i) {
                    if (i > 0) {
                        b.append(".");
                    }
                    b.append(keyParts[i]);
                }
                tags.put("path", b.toString());
            }
        }
    }
}
