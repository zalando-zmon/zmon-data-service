package de.zalando.zmon.dataservice.data;

import io.opentracing.contrib.apache.http.client.TracingHttpClientBuilder;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;

import java.util.concurrent.TimeUnit;

public class HttpClientFactory {
    public static HttpClient getHttpClient(int socketTimeout, int timeout, int maxConnections, long connectionsTimeToLive) {
        final RequestConfig config = RequestConfig.custom()
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(timeout)
                .build();
        return new TracingHttpClientBuilder()
                .setMaxConnPerRoute(maxConnections)
                .setMaxConnTotal(maxConnections)
                .setConnectionTimeToLive(connectionsTimeToLive, TimeUnit.MILLISECONDS)
                .setDefaultRequestConfig(config)
                .build();
    }

    public  static Executor getExecutor(int socketTimeout, int timeout, int maxConnections, long connectionsTimeToLive) {
        final HttpClient httpClient = getHttpClient(socketTimeout, timeout, maxConnections, connectionsTimeToLive);
        return Executor.newInstance(httpClient);
    }
}
