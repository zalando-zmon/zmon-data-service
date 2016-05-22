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

	public static HttpClient getHttpClient(int socketTimeout, int timeout, int maxConnections) {
		RequestConfig config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(timeout).build();
		return HttpClients.custom().setMaxConnPerRoute(maxConnections).setMaxConnTotal(maxConnections).setDefaultRequestConfig(config).build();
	}

	@Override
	public String allActiveAlertDefinitions(String query) throws URISyntaxException, IOException {

		final Executor executor = Executor.newInstance(getHttpClient(100, 5000, 1));

		URI uri = new URIBuilder().setPath(config.getProxyControllerUrl() + "/checks/all-active-alert-definitions")
				.setParameter("query", query).build();

		return executor.execute(Request.Get(uri)).returnContent().asString();
	}

	@Override
	public String allActiveCheckDefinitions(String query) throws URISyntaxException, IOException {

		final Executor executor = Executor.newInstance(getHttpClient(100, 5000, 1));

		URI uri = new URIBuilder().setPath(config.getProxyControllerUrl() + "/checks/all-active-check-definitions")
				.setParameter("query", query).build();

		return executor.execute(Request.Get(uri)).returnContent().asString();
	}

}
