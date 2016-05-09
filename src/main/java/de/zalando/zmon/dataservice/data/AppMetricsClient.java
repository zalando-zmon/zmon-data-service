package de.zalando.zmon.dataservice.data;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.fluent.Async;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.zalando.zmon.dataservice.components.DefaultObjectMapper;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

/**
 * Created by jmussler on 05.12.15.
 */

@Service
public class AppMetricsClient {

    private static final Logger LOG = LoggerFactory.getLogger(AppMetricsClient.class);

    private final List<String> serviceHosts;
    private final int serverPort;

    private final ObjectMapper mapper;

    private final ExecutorService asyncExecutorPool = Executors.newFixedThreadPool(15);

    private final Async async;

    @Autowired
    public AppMetricsClient(DataServiceConfigProperties config, @DefaultObjectMapper ObjectMapper defaultObjectMapper) {
        serviceHosts = config.getRestMetricHosts();
        serverPort = config.getRestMetricPort();
        this.mapper = defaultObjectMapper;
        async = Async.newInstance().use(asyncExecutorPool);

        LOG.info("App metric cache config: hosts {} port {}", serviceHosts, serverPort);
    }

    public void receiveData(Map<Integer, List<CheckData>> data) {
        for (int i = 0; i < serviceHosts.size(); ++i) {
            if (!data.containsKey(i) || data.get(i).size() <= 0)
                continue;

            try {
                Request r = Request
                        .Post("http://" + serviceHosts.get(i) + ":" + serverPort + "/api/v1/rest-api-metrics/")
                        .addHeader("Cookie", "metric_cache="+i)
                        .bodyString(mapper.writeValueAsString(data.get(i)), ContentType.APPLICATION_JSON);
                async.execute(r);
            } catch (IOException ex) {
                LOG.error("Failed to serialize check data", ex);
            }
        }
    }
}
