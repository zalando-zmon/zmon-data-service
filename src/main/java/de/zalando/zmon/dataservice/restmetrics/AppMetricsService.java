package de.zalando.zmon.dataservice.restmetrics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.zalando.zmon.dataservice.DataServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by jmussler on 05.12.15.
 */

@Service
public class AppMetricsService {

    private static final Logger LOG = LoggerFactory.getLogger(AppMetricsService.class);

    private List<String> serviceHosts;

    private HashMap<String, ApplicationVersion> appVersions = new HashMap<>();

    @Autowired
    public AppMetricsService(DataServiceConfig config) {
        serviceHosts = config.getRest_metric_hosts();
    }

    public void pushMetric(String applicationId, String applicationVersion, String entityId, long ts, JsonNode checkResult) {
        if(serviceHosts==null || serviceHosts.size()<=0) {
            // zmon actuator map looks like:
            // ep - method - status - metric

            Iterator<Map.Entry<String, JsonNode>> endpoints = ((ObjectNode)checkResult).fields();
            while(endpoints.hasNext()) {
                Map.Entry<String, JsonNode> endpoint = endpoints.next();
                String path = endpoint.getKey();

                Iterator<Map.Entry<String, JsonNode>> methods = ((ObjectNode) endpoint).fields();
                while(methods.hasNext()) {
                    Map.Entry<String, JsonNode> methodEntry = methods.next();
                    String method = methodEntry.getKey();

                    Iterator<Map.Entry<String, JsonNode>> statusCodes = ((ObjectNode) methodEntry).fields();
                    while(statusCodes.hasNext()) {
                        Map.Entry<String, JsonNode> metricEntry = statusCodes.next();
                        if(metricEntry.getValue().has("99th") && metricEntry.getValue().has("mRate")) {
                            storeMetric(applicationId, applicationVersion, entityId, path, method, Integer.parseInt(metricEntry.getKey()),
                                    ts,
                                    metricEntry.getValue().get("mRate").asDouble(),
                                    metricEntry.getValue().get("99th").asDouble());
                        }
                    }
                }
            }
        }
        else {
            // dirty hack forwarding request to host where metrics for this app are stored
            int k = applicationId.hashCode() % serviceHosts.size();
        }
    }

    public VersionResult getAggrMetrics(String applicationId, String applicationVersion, long maxTs) {
        return appVersions.get(applicationId).getData(maxTs);
    }

    private void storeMetric(String applicationId, String applicationVersion, String entityId, String path, String method, int status, long ts, double rate, double latency) {
        ApplicationVersion v = appVersions.get(applicationId); // no versioning for now
        if(null==v) {
            synchronized (this) {
                v = appVersions.get(applicationId);
                if(null==v) {
                    LOG.info("Adding application version {} {}", applicationId, applicationVersion);
                    v = new ApplicationVersion(applicationId, applicationVersion);
                    appVersions.put(applicationId, v);
                }
            }
        }
        v.addDataPoint(entityId, path, method, status, ts, rate, latency);
    }
}
