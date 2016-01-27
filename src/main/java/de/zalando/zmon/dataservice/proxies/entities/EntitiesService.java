package de.zalando.zmon.dataservice.proxies.entities;

import java.io.IOException;
import java.net.URISyntaxException;

public interface EntitiesService {

	String deleteEntity(String id) throws URISyntaxException, IOException;

	String getEntities(String query) throws URISyntaxException, IOException;

	String addEntities(String entities) throws URISyntaxException, IOException;

}
