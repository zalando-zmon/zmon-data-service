package de.zalando.zmon.dataservice.proxies.checks;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

public class DefaultChecksService implements ChecksService {

	private final DataServiceConfigProperties config;

	public DefaultChecksService(DataServiceConfigProperties config) {
		this.config = config;
	}

	@Override
	public String allActiveAlertDefinitions(String query) throws URISyntaxException, IOException {
		URI uri = new URIBuilder().setPath(config.getProxyControllerUrl() + "/checks/all-active-alert-definitions")
				.setParameter("query", query).build();
		return Request.Get(uri).useExpectContinue().execute().returnContent().asString();
	}

	@Override
	public String allActiveCheckDefinitions(String query) throws URISyntaxException, IOException {
		URI uri = new URIBuilder().setPath(config.getProxyControllerUrl() + "/checks/all-active-check-definitions")
				.setParameter("query", query).build();
		return Request.Get(uri).useExpectContinue().execute().returnContent().asString();
	}

}
