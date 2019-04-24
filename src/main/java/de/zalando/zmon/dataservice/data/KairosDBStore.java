package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.google.common.collect.ImmutableSet;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;


/**
 * Created by jmussler on 5/8/15.
 */
@Service
public class KairosDBStore {

    private static final Logger LOG = LoggerFactory.getLogger(KairosDBStore.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final DataServiceConfigProperties config;

    private final DataPointsQueryStore dataPointsQueryStore;

    private final Set<String> entityTagFields;
    // adding alias,account_alias,cluster_alias due to legacy, and should be exclusive anyways
    private final static Set<String> DEFAULT_ENTITY_TAG_FIELDS = new HashSet<>(
            ImmutableSet.of("application_id", "application_version", "stack_name", "stack_version", "application",
                    "version", "account_alias", "cluster_alias", "alias", "namespace", "component"));

    private static final String REPLACE_CHAR = "_";
    private static final Pattern KAIROSDB_INVALID_TAG_CHARS = Pattern.compile("[?@:=\\[\\]]");

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

    private final DataServiceMetrics metrics;
    private final int resultSizeWarning;

    private static class DataPoint {
        public String name;
        public List<ArrayNode> datapoints = new LinkedList<>();
        public Map<String, String> tags = new HashMap<>();
    }

    @Autowired
    public KairosDBStore(DataServiceConfigProperties config, DataServiceMetrics metrics, DataPointsQueryStore dataPointsQueryStore) {
        this.metrics = metrics;
        this.config = config;
        this.dataPointsQueryStore = dataPointsQueryStore;
        this.resultSizeWarning = config.getResultSizeWarning();

        if (null == config.getKairosdbTagFields() || config.getKairosdbTagFields().size() == 0) {
            this.entityTagFields = DEFAULT_ENTITY_TAG_FIELDS;
        } else {
            this.entityTagFields = new HashSet<>(config.getKairosdbTagFields());
        }
    }

    public static String extractMetricName(String key) {
        if (null == key || "".equals(key)) return null;
        String[] keyParts = key.split("\\.");
        String metricName = keyParts[keyParts.length - 1];
        if ("".equals(metricName)) {
            metricName = keyParts[keyParts.length - 2];
        }
        return metricName;
    }

    public Map<String, String> getTags(String key, String entityId, Map<String, String> entity) {
        Map<String, String> tags = new HashMap<>();
        tags.put("entity", KAIROSDB_INVALID_TAG_CHARS.matcher(entityId).replaceAll(REPLACE_CHAR));

        for (String field : entityTagFields) {
            if (entity.containsKey(field)) {
                String fieldValue = entity.get(field);
                if (null != fieldValue && !"".equals(fieldValue)) {
                    tags.put(field, entity.get(field));
                }
            }
        }

        if (null != key && !"".equals(key)) {
            tags.put("key", KAIROSDB_INVALID_TAG_CHARS.matcher(key).replaceAll(REPLACE_CHAR));
        }

        String metricName = extractMetricName(key);
        if (null != metricName) {
            tags.put("metric", KAIROSDB_INVALID_TAG_CHARS.matcher(metricName).replaceAll(REPLACE_CHAR));
        }

        return tags;
    }

    void store(WorkerResult wr) {
        if (!config.isKairosdbEnabled()) {
            return;
        }

        if (wr == null || wr.results == null || wr.results.isEmpty()) {
            LOG.warn("Received a request with invalid results: {}", wr);
            return;
        }

        try {
            List<DataPoint> points = new LinkedList<>();
            for (CheckData cd : wr.results) {

                if (!cd.isSampled) {
                    LOG.debug("Dropping non-sampled metrics for checkid={}", cd.checkId);
                    continue;
                }

                final Map<String, NumericNode> values = new HashMap<>();
                final String timeSeries = "zmon.check." + cd.checkId;

                Double ts = cd.checkResult.get("ts").asDouble();
                ts = ts * 1000.;
                Long tsL = ts.longValue();

                fillFlatValueMap(values, "", cd.checkResult.get("value"));

                int cdResultSize = 0;
                for (Map.Entry<String, NumericNode> e : values.entrySet()) {
                    DataPoint p = new DataPoint();
                    p.name = timeSeries;

                    p.tags.putAll(getTags(e.getKey(), cd.entityId, cd.entity));

                    // handle zmon actuator metrics and extract the http status code into its own field
                    // put the first character of the status code into "status group" sg, this is only for easy kairosdb query
                    if (config.getActuatorMetricChecks().contains(cd.checkId)) {
                        final String[] keyParts = e.getKey().split("\\.");

                        if (keyParts.length >= 3 && "health".equals(keyParts[0]) && "200".equals(keyParts[2])) {
                            // remove the 200 health check data points, with 1/sec * instances with elb checks they just confuse
                            continue;
                        }

                        if (keyParts.length >= 3) {
                            final String statusCode = keyParts[keyParts.length - 2];
                            p.tags.put("sc", statusCode);
                            p.tags.put("sg", statusCode.substring(0, 1));

                            if (keyParts.length >= 4) {
                                StringBuilder b = new StringBuilder();
                                for (int i = 0; i < keyParts.length - 3; ++i) {
                                    if (i > 0) {
                                        b.append(".");
                                    }
                                    b.append(keyParts[i]);
                                }

                                p.tags.put("path", b.toString());
                            }
                        }
                    }

                    ArrayNode arrayNode = mapper.createArrayNode();
                    arrayNode.add(tsL);
                    arrayNode.add(e.getValue());

                    p.datapoints.add(arrayNode);
                    points.add(p);
                    cdResultSize += 1
                }

                if (cdResultSize > resultSizeWarning) {
                    LOG.warn("result size warning: check={} data-points={} entity={}", cd.checkId, cdResultSize, cd.entityId);
                }
            }

            if (points.size()>0){
                metrics.incKairosDBDataPoints(points.size());

                String query = mapper.writeValueAsString(points);
                if (config.isLogKairosdbRequests()) {
                    LOG.info("KairosDB Query: {}", query);
                }

                // Store datapoints query!
                int err = dataPointsQueryStore.store(query);
                if (err > 0) {
                    metrics.markKairosHostErrors(err);
                }
            }

        } catch (IOException ex) {
            if (config.isLogKairosdbErrors()) {
                LOG.error("KairosDB write path failed", ex);
            }
            metrics.markKairosError();
        }
    }
}
