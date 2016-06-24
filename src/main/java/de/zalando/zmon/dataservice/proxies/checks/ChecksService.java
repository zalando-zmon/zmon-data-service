package de.zalando.zmon.dataservice.proxies.checks;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

public interface ChecksService {

	String allActiveAlertDefinitions(Optional<String> token, String query) throws URISyntaxException, IOException;

	String allActiveCheckDefinitions(Optional<String> token, String query) throws URISyntaxException, IOException;

}
