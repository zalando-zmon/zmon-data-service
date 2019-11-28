package de.zalando.zmon.dataservice.proxies;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import de.zalando.zmon.dataservice.data.HttpClientFactory;
import de.zalando.zmon.dataservice.oauth2.BearerToken;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import java.io.IOException;
import java.util.Optional;


public class ControllerProxy {
    private final CircuitBreakerFactory cbFactory;
    protected final DataServiceConfigProperties config;
    protected final Executor executor;

    public ControllerProxy(CircuitBreakerFactory cbFactory, DataServiceConfigProperties config) {
        this.cbFactory = cbFactory;
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

    protected String proxy(Request request, Optional<String> token) {
        if (config.isProxyControllerOauth2()) {
            BearerToken.inject(request, token);
        }
        return cbFactory.create("proxy").run(
                () -> {
                    try {
                        return executor.execute(request).returnContent().asString();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    protected String proxyForLastModifiedHeader(Request request, Optional<String> token) {
        if (config.isProxyControllerOauth2()) {
            BearerToken.inject(request, token);
        }

        return cbFactory.create("proxy").run(
                () -> {
                    try {
                        return executor.execute(request).returnResponse().getHeaders("Last-Modified")[0].getValue();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
