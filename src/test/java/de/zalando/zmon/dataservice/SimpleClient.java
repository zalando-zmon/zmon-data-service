package de.zalando.zmon.dataservice;

import java.util.Map;

import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

class SimpleClient {

	private RestOperations restOperations;

	SimpleClient() {
		this(new RestTemplate());
	}

	SimpleClient(RestOperations restOperations) {
		this.restOperations = restOperations;
	}

	public Map<String, Object> getTokenInfo() {
		return restOperations.getForEntity("http://localhost:9999/oauth2/tokeninfo", Map.class).getBody();
	}

}
