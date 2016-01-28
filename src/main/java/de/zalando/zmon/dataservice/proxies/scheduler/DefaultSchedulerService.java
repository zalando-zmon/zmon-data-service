package de.zalando.zmon.dataservice.proxies.scheduler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

/**
 * 
 * @author jbellmann
 *
 */
public class DefaultSchedulerService implements SchedulerService {

	private final DataServiceConfigProperties config;

	public DefaultSchedulerService(DataServiceConfigProperties config) {
		this.config = config;
	}

	@Override
	public String trialRun(String dc) throws IOException, URISyntaxException {
		URI uri = new URIBuilder().setPath(config.getProxySchedulerUrl() + "/trial-runs/" + dc + "/").build();
		return Request.Get(uri).useExpectContinue().execute().returnContent().asString();
	}

	@Override
	public String instantEvaluations(String dc) throws IOException, URISyntaxException {
		URI uri = new URIBuilder().setPath(config.getProxySchedulerUrl() + "/instant-evaluations/" + dc + "/")
				.build();
		return Request.Get(uri).useExpectContinue().execute().returnContent().asString();
	}

}
