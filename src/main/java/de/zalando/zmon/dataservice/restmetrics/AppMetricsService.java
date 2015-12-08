package de.zalando.zmon.dataservice.restmetrics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.zalando.zmon.dataservice.CheckData;
import de.zalando.zmon.dataservice.DataServiceConfig;
import org.apache.http.client.fluent.Async;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jmussler on 05.12.15.
 */

@Service
public class AppMetricsService {

    private static final Logger LOG = LoggerFactory.getLogger(AppMetricsService.class);

    private List<String> serviceHosts;

    private HashMap<String, ApplicationVersion> appVersions = new HashMap<>();

    private final String localHostName;
    private int localPartition = -1;

    private final static ObjectMapper mapper = new ObjectMapper();

    private final ExecutorService asyncExecutorPool = Executors.newFixedThreadPool(5);

    @Autowired
    public AppMetricsService(DataServiceConfig config) throws IOException {
        serviceHosts = config.getRest_metric_hosts();
        localHostName = InetAddress.getLocalHost().getHostName();
        for(int i = 0; i < serviceHosts.size(); ++i) {
            if(serviceHosts.get(i).equals(localHostName)) {
                localPartition = i;
                break;
            }
        }

        LOG.info("Setting local partition to {}", localPartition);
        LOG.info("Host names {}", serviceHosts);
    }

    public void storeData(List<CheckData> data) {
        for(CheckData d: data) {
            Double ts = d.check_result.get("ts").asDouble();
            ts = ts * 1000.;
            Long tsL = ts.longValue();
            pushMetric(d.entity.get("application_id"), d.entity.get("application_version"),d.entity_id, tsL, d.check_result.get("value"));
        }
    }

    public void receiveData(Map<Integer, List<CheckData>> data) {
        // store local data
        if(data.containsKey(localPartition)) {
            storeData(data.get(localPartition));
        }

        Async async = Async.newInstance().use(asyncExecutorPool);
        for(int i = 0; i<serviceHosts.size(); ++i) {
            if(localPartition==i) continue;
            if(!data.containsKey(i) || data.get(i).size()<=0) continue;
            try {
                Request r = Request.Post(serviceHosts.get(i)+"/api/v1/rest-api-metrics/").bodyString(mapper.writeValueAsString(data.get(i)), ContentType.APPLICATION_JSON);
                async.execute(r);
            }
            catch(IOException ex) {
                LOG.error("Failed to serialize check data", ex);
            }
        }
    }

    public void pushMetric(String applicationId, String applicationVersion, String entityId, long ts, JsonNode checkResult) {
        Iterator<Map.Entry<String, JsonNode>> endpoints = ((ObjectNode)checkResult).fields();
        while(endpoints.hasNext()) {
            Map.Entry<String, JsonNode> endpoint = endpoints.next();
            String path = endpoint.getKey();

            Iterator<Map.Entry<String, JsonNode>> methods = ((ObjectNode) endpoint.getValue()).fields();
            while(methods.hasNext()) {
                Map.Entry<String, JsonNode> methodEntry = methods.next();
                String method = methodEntry.getKey();

                Iterator<Map.Entry<String, JsonNode>> statusCodes = ((ObjectNode) methodEntry.getValue()).fields();
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

    public VersionResult getAggrMetrics(String applicationId, String applicationVersion, long maxTs) {
        if(!appVersions.containsKey(applicationId)) {
            return null;
        }
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
