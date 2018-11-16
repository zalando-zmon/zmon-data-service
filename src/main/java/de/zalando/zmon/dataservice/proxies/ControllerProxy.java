package de.zalando.zmon.dataservice.proxies;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import de.zalando.zmon.dataservice.data.HttpClientFactory;
import de.zalando.zmon.dataservice.oauth2.BearerToken;
import io.opentracing.contrib.apache.http.client.TracingHttpClientBuilder;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


public class ControllerProxy {
    protected final DataServiceConfigProperties config;
    protected final Executor executor;

    public ControllerProxy(DataServiceConfigProperties config) {
        this.config = config;
        executor = getExecutor(config);
    }

    /**
     * @return HTTP executor to use only once (no connection pooling)
     */
    private static Executor getExecutor(DataServiceConfigProperties config) {
        final int maxConnections = 100;
        return HttpClientFactory.getExecutor(
                config.getProxyControllerSocketTimeout(),
                config.getProxyControllerConnectTimeout(),
                maxConnections,
                config.getConnectionsTimeToLive()
        );
    }

    protected URIBuilder uri(String path)  {
        return new URIBuilder().setPath(config.getProxyControllerUrl() + path);
    }

    protected String proxy(Request request, Optional<String> token) throws IOException {
        if (config.isProxyControllerOauth2()) {
            BearerToken.inject(request, token);
        }
        return executor.execute(request).returnContent().asString();
    }

    protected String proxyForLastModifiedHeader(Request request, Optional<String> token) throws IOException {
        if (config.isProxyControllerOauth2()) {
            BearerToken.inject(request, token);
        }

        return executor.execute(request).returnResponse().getHeaders("Last-Modified")[0].getValue();
    }
}
