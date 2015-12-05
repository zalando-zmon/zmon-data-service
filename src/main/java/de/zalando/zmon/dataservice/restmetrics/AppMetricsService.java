package de.zalando.zmon.dataservice.restmetrics;

import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * Created by jmussler on 05.12.15.
 */

@Service
public class AppMetricsService {

    private HashMap<String, ApplicationVersion> appVersions = new HashMap<>();

    public void pushMetric(String applicationId, String applicationVersion, String entityId, String path, int status, long ts, double rate, double latency) {
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
        v.addDataPoint(entityId, path, status, ts, rate, latency);
    }
}
