package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import de.zalando.zmon.dataservice.utils.JsonUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Created by jmussler on 5/8/15.
 */
@Service
public class KairosDBStore {

    private static final Logger LOG = LoggerFactory.getLogger(KairosDBStore.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final DataServiceConfigProperties config;

    private final static Set<String> TAG_FIELDS = new HashSet<>(
        Arrays.asList("application_id", "application_version", "stack_name", "stack_version", "kube_service_name"));

    private final DataServiceMetrics metrics;
    private final Executor executor;
    private final int resultSizeWarning;

    private static class DataPoint {
        public String name;
        public List<ArrayNode> datapoints = new LinkedList<>();
        public Map<String, String> tags = new HashMap<>();
    }

    public static HttpClient getHttpClient(int socketTimeout, int timeout, int maxConnections) {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(timeout).build();
        return HttpClients.custom().setMaxConnPerRoute(maxConnections).setMaxConnTotal(maxConnections).setDefaultRequestConfig(config).build();
    }

    @Autowired
    public KairosDBStore(DataServiceConfigProperties config, DataServiceMetrics metrics) {
        this.metrics = metrics;
        this.config = config;
        this.resultSizeWarning = config.getResultSizeWarning();


        if (config.isKairosdbEnabled()) {
            LOG.info("KairosDB settings connections={} socketTimeout={} timeout={}", config.getKairosdbConnections(), config.getKairosdbSockettimeout(), config.getKairosdbTimeout());

            executor = Executor.newInstance(getHttpClient(config.getKairosdbSockettimeout(), config.getKairosdbTimeout(), config.getKairosdbConnections()));
        } else {
            LOG.info("KairosDB is disabled.");

            executor = null;
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

    private static final String REPLACE_CHAR = "_";
    private static final Pattern KAIROSDB_INVALID_TAG_CHARS = Pattern.compile("[?@:=\\[\\]]");

    public static Map<String, String> getTags(String key, String entityId, Map<String, String> entity) {
        Map<String, String> tags = new HashMap<>();
        tags.put("entity", KAIROSDB_INVALID_TAG_CHARS.matcher(entityId).replaceAll(REPLACE_CHAR));

        for (String field : TAG_FIELDS) {
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

    void store(WorkerResult workerResult) {

        if (!config.isKairosdbEnabled()) {
            return;
        }

        if(workerResult == null || workerResult.results == null || workerResult.results.isEmpty()) {
            LOG.warn("Received a request with invalid results: {}", workerResult);
            return;
        }

        try {
            List<DataPoint> dataPoints = new LinkedList<>();

            for (CheckData checkData : workerResult.results) {
                final String timeSeries = "zmon.check." + checkData.check_id;

                String worker = "";
                if (checkData.check_result.has("worker")) {
                    worker = checkData.check_result.get("worker").asText();
                }

                Double ts = checkData.check_result.get("ts").asDouble();
                ts = ts * 1000.;
                Long tsL = ts.longValue();

                if (checkData.check_result.get("value") != null) {

                    final Map<String, JsonNode> flatMap = JsonUtils.flatMapJsonNode(checkData.check_result.get("value"));
                    final Map<String, NumericNode> values = JsonUtils.flatMapJsonNumericNodes(flatMap);

                    for (Map.Entry<String, NumericNode> entry : values.entrySet()) {

                        DataPoint point = new DataPoint();
                        point.name = timeSeries;
                        point.tags.putAll(getTags(entry.getKey(), checkData.entity_id, checkData.entity));

                        // handle zmon actuator metrics and extract the http status code into its own field
                        // put the first character of the status code into "status group" sg, this is only for easy kairosdb query
                        if (config.getActuatorMetricChecks().contains(checkData.check_id)) {
                            final String[] keyParts = entry.getKey().split("\\.");

                            if (keyParts.length >= 3 && "health".equals(keyParts[0]) && "200".equals(keyParts[2])) {
                                // remove the 200 health check data points, with 1/sec * instances with elb checks they just confuse
                                continue;
                            }

                            if (keyParts.length >= 3) {
                                final String statusCode = keyParts[keyParts.length - 2];
                                point.tags.put("sc", statusCode);
                                point.tags.put("sg", statusCode.substring(0, 1));

                                if (keyParts.length >= 4) {

                                    StringBuilder b = new StringBuilder();
                                    for (int i = 0; i < keyParts.length - 3; ++i) {
                                        if (i > 0) {
                                            b.append(".");
                                        }
                                        b.append(keyParts[i]);
                                    }

                                    point.tags.put("path", b.toString());
                                }
                            }
                        }

                        if (null != worker && !"".equals(worker)) {
                            point.tags.put("worker", worker);
                        }

                        ArrayNode arrayNode = mapper.createArrayNode();
                        arrayNode.add(tsL);
                        arrayNode.add(entry.getValue());

                        point.datapoints.add(arrayNode);
                        dataPoints.add(point);
                    }

                    if (dataPoints.size() > resultSizeWarning) {
                        LOG.warn("result size warning: check={} data-points={} entity={}", checkData.check_id, dataPoints.size(), checkData.entity_id);
                    }
                }
            }

            metrics.incKairosDBDataPoints(dataPoints.size());

            String query = mapper.writeValueAsString(dataPoints);
            if (config.isLogKairosdbRequests()) {
                LOG.info("KairosDB Query: {}", query);
            }

            for (List<String> urls : config.getKairosdbWriteUrls()) {
                // api is per check id, but for now we take the first one
                final int index = workerResult.results.get(0).check_id % urls.size();
                final String url = urls.get(index);

                try {
                    executor.execute(Request.Post(url + "/api/v1/datapoints")
                            .bodyString(query, ContentType.APPLICATION_JSON))
                            .returnContent()
                            .asString();
                } catch (IOException ex) {
                    if (config.isLogKairosdbErrors()) {
                        LOG.error("KairosDB write failed url={}", url, ex);
                    }
                    metrics.markKairosHostError();
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
