package de.zalando.zmon.dataservice.proxies.entities;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

public interface EntitiesService {
	String deleteEntity(Optional<String> token, String id) throws URISyntaxException, IOException;

	String getEntities(Optional<String> token, String query) throws URISyntaxException, IOException;

	String addEntities(Optional<String> token, String entities) throws URISyntaxException, IOException;
}
