package de.zalando.zmon.dataservice.proxies.entities;

import java.util.Optional;

public class NoOpEntitiesService implements EntitiesService {

	@Override
	public String deleteEntity(Optional<String> token, String id) {
		return "";
	}

	@Override
	public String getEntities(Optional<String> token, String query) {
		return "";
	}

	@Override
	public String addEntities(Optional<String> token, String entities) {
		return "";
	}

}
