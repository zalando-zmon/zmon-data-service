package de.zalando.zmon.dataservice.proxies.entities;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.components.CustomObjectMapper;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jbellmann
 */
public class DefaultEntitiesService implements EntitiesService {

    private final Logger log = LoggerFactory.getLogger(DefaultEntitiesService.class);

    private final DataServiceConfigProperties config;
    private final ObjectMapper customObjectMapper;
    private final DataServiceMetrics metrics;
    private final boolean oauth2Enabled;

    public DefaultEntitiesService(@CustomObjectMapper ObjectMapper customObjectMapper, DataServiceConfigProperties config, DataServiceMetrics metrics) {
        this.customObjectMapper = customObjectMapper;
        this.config = config;
        this.metrics = metrics;
        this.oauth2Enabled = config.isProxyControllerOauth2();
        log.info("Entity service proxy: {}", config.getProxyControllerBaseUrl());
    }

    /**
     * @return HTTP executor to use only once (no connection pooling)
     */
    private Executor getExecutor() {
        final int maxConnections = 1;
        final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(config.getProxyControllerSocketTimeout()).setConnectTimeout(config.getProxyControllerConnectTimeout()).build();
        final HttpClient httpClient = HttpClients.custom().setMaxConnPerRoute(maxConnections).setMaxConnTotal(maxConnections).setDefaultRequestConfig(requestConfig).build();
        final Executor executor = Executor.newInstance(httpClient);
        return executor;
    }

    @Override
    public String deleteEntity(Optional<String> token, String id) throws URISyntaxException, IOException {
        URI uri = new URIBuilder().setPath(config.getProxyControllerUrl() + "/entities/" + id + "/").build();
        Request r = Request.Delete(uri);
        if (oauth2Enabled && token.isPresent()) {
            r.addHeader("Authorization", "Bearer " + token);
        }
        return getExecutor().execute(r).returnContent().asString();
    }

    @Override
    public String getEntities(Optional<String> token, String query) throws URISyntaxException, IOException {
        URI uri = new URIBuilder().setPath(config.getProxyControllerUrl() + "/entities/").setParameter("query", query).build();

        Request r = Request.Get(uri);
        if (oauth2Enabled && token.isPresent()) {
            r.addHeader("Authorization", "Bearer " + token);
        }
        return getExecutor().execute(r).returnContent().asString();
    }

    @Override
    public String addEntities(Optional<String> token, String entities) throws URISyntaxException, IOException {
        JsonNode node = customObjectMapper.readTree(entities);
        if (node.has("infrastructure_account")) {
            String id = node.get("infrastructure_account").textValue();
            metrics.markEntity(id, 1);
        }

        URI uri = new URIBuilder().setPath(config.getProxyControllerUrl() + "/entities/").build();

        Request r = Request.Put(uri).bodyString(customObjectMapper.writeValueAsString(node), ContentType.APPLICATION_JSON);
        if (oauth2Enabled && token.isPresent()) {
            r.addHeader("Authorization", "Bearer " + token);
        }

        return getExecutor().execute(r).returnContent().asString();
    }
}
