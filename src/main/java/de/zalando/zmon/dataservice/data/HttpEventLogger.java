package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.JsonNode;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.EventType;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.apache.http.client.fluent.Async;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jmussler on 10.06.16.
 */
@Component
public class HttpEventLogger {

    private final boolean enabled;

    private final String forwardUrl;
    private final Executor executor;

    private DataServiceMetrics metrics;
    private final Async async;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(HttpEventLogger.class);

    private static class HttpEvent {
        public Map<String, JsonNode> attributes;

        public Date time;
        public int typeId;

        public HttpEvent(Date time, EventType type, JsonNode[] values) {
            this.time = time;
            this.typeId = type.getId();
            this.attributes = new TreeMap<>();

            for (int i = 0; i < type.getFieldNames().size(); ++i) {
                if (i < values.length) {
                    attributes.put(type.getFieldNames().get(i), values[i]);
                } else {
                    attributes.put(type.getFieldNames().get(i), null);
                }
            }
        }
    }

    @Autowired
    public HttpEventLogger(DataServiceMetrics metrics, DataServiceConfigProperties config) {
        this.metrics = metrics;
        enabled = config.isEventlogEnabled();
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);

        if (enabled) {
            forwardUrl = config.getEventlogUrl() + "/api/v1";
            log.info("EventLog enabled: {}", forwardUrl);
            executor = HttpClientFactory.getExecutor(
                    config.getEventlogSocketTimeout(),
                    config.getEventlogTimeout(),
                    config.getEventlogConnections(),
                    config.getConnectionsTimeToLive()
            );
            ExecutorService threadPool = Executors.newFixedThreadPool(config.getEventlogPoolSize());
            async = Async.newInstance().use(threadPool).use(executor);
        } else {
            log.info("EventLog disabled");
            forwardUrl = null;
            async = null;
            executor = null;
        }
    }

    public void log(EventType type, JsonNode... values) {
        if (!enabled) {
            return;
        }

        try {
            Request request = Request.Post(forwardUrl + "/")
                    .bodyString("[" + mapper.writeValueAsString(new HttpEvent(new Date(), type, values)) + "]", ContentType.APPLICATION_JSON);

            async.execute(request, new FutureCallback<Content>() {

                public void failed(final Exception ex) {
                    metrics.markEventLogError();
                }

                public void completed(final Content content) {
                }

                public void cancelled() {
                }

            });
        } catch (Throwable t) {
            log.error("EventLog write failed: {}", t.getMessage());
            metrics.markEventLogError();
        }
    }
}
