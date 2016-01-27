package de.zalando.zmon.dataservice.proxies.entities;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.components.CustomObjectMapper;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

/**
 * 
 * @author jbellmann
 *
 */
public class DefaultEntitiesService implements EntitiesService {

	
	private final DataServiceConfigProperties config;
	private final ObjectMapper customObjectMapper;
	private final DataServiceMetrics metrics;

	public DefaultEntitiesService(@CustomObjectMapper ObjectMapper customObjectMapper, DataServiceConfigProperties config, DataServiceMetrics metrics) {
		this.customObjectMapper = customObjectMapper;
		this.config = config;
		this.metrics = metrics;
	}

	@Override
	public String deleteEntity(String id) throws URISyntaxException, IOException{
        URI uri = new URIBuilder().setPath(config.getProxyControllerUrl() + "/entities/"+id+'/').build();

        final Executor executor = Executor.newInstance();

        return executor.execute(Request.Delete(uri).useExpectContinue()).returnContent().asString();
	}

	@Override
	public String getEntities(String query) throws URISyntaxException, IOException {
		URI uri = new URIBuilder().setPath(config.getProxyControllerUrl() + "/entities/").setParameter("query",query).build();
        final String r = Request.Get(uri).useExpectContinue().execute().returnContent().asString();
        return r;
	}

	@Override
	public String addEntities(String entities) throws URISyntaxException, IOException {
		JsonNode node = customObjectMapper.readTree(entities);
		if (node.has("infrastructure_account")) {
			String id = node.get("infrastructure_account").textValue();
			metrics.markEntity(id, 1);
		}

		URI uri = new URIBuilder().setPath(config.getProxyControllerUrl() + "/entities/").build();

		final Executor executor = Executor.newInstance();

		String r = executor.execute(Request.Put(uri).useExpectContinue()
				.bodyString(customObjectMapper.writeValueAsString(node), ContentType.APPLICATION_JSON)).returnContent()
				.asString();
		return r;
	}

}
