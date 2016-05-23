package de.zalando.zmon.dataservice.proxies.checks;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.apache.http.impl.client.HttpClients;

public class DefaultChecksService implements ChecksService {

    private final DataServiceConfigProperties config;

    public DefaultChecksService(DataServiceConfigProperties config) {
        this.config = config;
    }

    /**
     * @return HTTP executor to use only once (no conection pooling)
     */
    private Executor getExecutor() {
        final int maxConnections = 1;
        final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(config.getProxyControllerSocketTimeout()).setConnectTimeout(config.getProxyControllerConnectTimeout()).build();
        final HttpClient httpClient = HttpClients.custom().setMaxConnPerRoute(maxConnections).setMaxConnTotal(maxConnections).setDefaultRequestConfig(requestConfig).build();
        final Executor executor = Executor.newInstance(httpClient);
        return executor;
    }

    @Override
    public String allActiveAlertDefinitions(String query) throws URISyntaxException, IOException {
        URI uri = new URIBuilder().setPath(config.getProxyControllerUrl() + "/checks/all-active-alert-definitions")
                .setParameter("query", query).build();

        return getExecutor().execute(Request.Get(uri)).returnContent().asString();
    }

    @Override
    public String allActiveCheckDefinitions(String query) throws URISyntaxException, IOException {
        URI uri = new URIBuilder().setPath(config.getProxyControllerUrl() + "/checks/all-active-check-definitions")
                .setParameter("query", query).build();

        return getExecutor().execute(Request.Get(uri)).returnContent().asString();
    }

}
