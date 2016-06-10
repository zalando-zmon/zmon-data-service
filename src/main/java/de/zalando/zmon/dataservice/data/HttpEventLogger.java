package de.zalando.zmon.dataservice.data;

import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.EventType;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.apache.http.client.fluent.Async;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Autowired
    public HttpEventLogger(DataServiceMetrics metrics, DataServiceConfigProperties config) {
        this.metrics = metrics;
        enabled = config.isEventLogEnabled();

        if(enabled) {
            forwardUrl = config.getEventLogUrl() + "/api/v1/"
            executor = Executor.newInstance(ProxyWriter.getHttpClient(config.getDataProxySocketTimeout(), config.getDataProxyTimeout(), config.getDataProxyConnections()));
            ExecutorService threadpool = Executors.newFixedThreadPool(config.getDataProxyPoolSize());
            async = Async.newInstance().use(threadpool).use(executor);
        }
        else {
            forwardUrl = null;
            async = null;
            executor = null;
        }
    }

    public void log(EventType type, Object... values) {
        if(!enabled) {
            return;
        }

        try {



            Request request = Request.Put(forwardUrl + "/")
                    .bodyString(, ContentType.APPLICATION_JSON);

            async.execute(request, new FutureCallback<Content>() {

                public void failed(final Exception ex) {
                    metrics.markProxyError();
                }

                public void completed(final Content content) {
                }

                public void cancelled() {
                }

            });
        }
        catch (Exception ex) {
            metrics.markProxyError();
        }

    }
}
