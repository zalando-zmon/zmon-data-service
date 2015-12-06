package de.zalando.zmon.dataservice.restmetrics;

import com.fasterxml.jackson.databind.JsonNode;
import de.zalando.zmon.dataservice.DataServiceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * Created by jmussler on 05.12.15.
 */

@Service
public class AppMetricsService {

    private List<String> serviceHosts;

    private HashMap<String, ApplicationVersion> appVersions = new HashMap<>();

    @Autowired
    public AppMetricsService(DataServiceConfig config) {
        serviceHosts = config.getRest_metric_hosts();
    }

    public void pushMetric(String applicationId, String applicationVersion, String entityId, JsonNode plainResult) {
        if(serviceHosts==null || serviceHosts.size()<=0) {
            // zmon actuator map looks like:
            // ep - method - status - metric

            // loop over result and push rate/latency
            // storeMetric(applicationId, applicationVersion, entityId, plainResult);
        }
        else {
            // dirty hack forwarding request to host where metrics for this app are stored
            int k = applicationId.hashCode() % serviceHosts.size();
        }
    }

    public void getAggrMetrics(String applicationId, String applicationVersion, long maxTs) {
        // return local data or go to one of service hosts
    }

    private void storeMetric(String applicationId, String applicationVersion, String entityId, String path, String method, int status, long ts, double rate, double latency) {
        ApplicationVersion v = appVersions.get(applicationId); // no versioning for now
        if(null==v) {
            synchronized (this) {
                v = appVersions.get(applicationId);
                if(null==v) {
                    v = new ApplicationVersion();
                    appVersions.put(applicationId, v);
                }
            }
        }
        v.addDataPoint(entityId, path, method, status, ts, rate, latency);
    }
}
