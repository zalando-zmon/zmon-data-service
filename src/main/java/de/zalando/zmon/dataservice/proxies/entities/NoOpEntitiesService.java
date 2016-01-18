package de.zalando.zmon.dataservice.proxies.entities;

public class NoOpEntitiesService implements EntitiesService {

	@Override
	public String deleteEntity(String id) {
		return "";
	}

	@Override
	public String getEntities(String query) {
		return "";
	}

	@Override
	public String addEntities(String entities) {
		return "";
	}

}
