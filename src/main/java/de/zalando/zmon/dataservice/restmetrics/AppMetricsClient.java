package de.zalando.zmon.dataservice.restmetrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.zalando.zmon.dataservice.CheckData;
import de.zalando.zmon.dataservice.DataServiceConfig;
import org.apache.http.client.fluent.Async;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by jmussler on 05.12.15.
 */

@Service
public class AppMetricsClient {

    private static final Logger LOG = LoggerFactory.getLogger(AppMetricsClient.class);

    private final List<String> serviceHosts;
    private final int serverPort;

    private final static ObjectMapper mapper = new ObjectMapper();

    private final ExecutorService asyncExecutorPool = Executors.newFixedThreadPool(5);


    @Autowired
    public AppMetricsClient(DataServiceConfig config) throws IOException {
        serviceHosts = config.getRest_metric_hosts();
        serverPort = config.getRest_metric_port();

        LOG.info("App metric cache config: hosts {} port {}", serviceHosts, serverPort);
    }

    public void receiveData(Map<Integer, List<CheckData>> data) {
        Async async = Async.newInstance().use(asyncExecutorPool);
        for(int i = 0; i < serviceHosts.size(); ++i) {
            if(!data.containsKey(i) || data.get(i).size()<=0) continue;

            try {
                Request r = Request.Post("http://"+serviceHosts.get(i)+":"+ serverPort +"/api/v1/rest-api-metrics/").bodyString(mapper.writeValueAsString(data.get(i)), ContentType.APPLICATION_JSON);
                async.execute(r);
            }
            catch(IOException ex) {
                LOG.error("Failed to serialize check data", ex);
            }
        }
    }
}
