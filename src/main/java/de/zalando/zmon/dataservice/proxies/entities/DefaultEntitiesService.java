package de.zalando.zmon.dataservice.proxies.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.components.CustomObjectMapper;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import de.zalando.zmon.dataservice.proxies.ControllerProxy;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * @author jbellmann
 */
public class DefaultEntitiesService extends ControllerProxy implements EntitiesService {

    private final Logger log = LoggerFactory.getLogger(DefaultEntitiesService.class);

    private final ObjectMapper customObjectMapper;
    private final DataServiceMetrics metrics;

    public DefaultEntitiesService(@CustomObjectMapper ObjectMapper customObjectMapper, DataServiceConfigProperties config, DataServiceMetrics metrics) {
        super(config);
        this.customObjectMapper = customObjectMapper;
        this.metrics = metrics;
        log.info("Entity service proxy: {}", config.getProxyControllerBaseUrl());
    }

    @Override
    public String deleteEntity(Optional<String> token, String id) throws URISyntaxException, IOException {
        URI uri = uri("/entities/" + id + "/").build();
        return proxy(Request.Delete(uri), token);
    }

    @Override
    public String getEntities(Optional<String> token, String query) throws URISyntaxException, IOException {
        URI uri = uri("/entities/").setParameter("query", query).build();
        return proxy(Request.Get(uri), token);
    }

    @Override
    public String addEntities(Optional<String> token, String entities) throws URISyntaxException, IOException {
        JsonNode node = customObjectMapper.readTree(entities);
        if (node.has("infrastructure_account")) {
            String id = node.get("infrastructure_account").textValue();
            metrics.markEntity(id, 1);
        }

        URI uri = uri("/entities/").build();

        Request request = Request.Put(uri).bodyString(customObjectMapper.writeValueAsString(node), ContentType.APPLICATION_JSON);
        return proxy(request, token);
    }
}
