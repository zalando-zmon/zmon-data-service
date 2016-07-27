package de.zalando.zmon.dataservice.proxies.checks;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

public class NoOpChecksService implements ChecksService {

	@Override
	public String allActiveAlertDefinitions(Optional<String> token, String query) {
		return "";
	}

	@Override
	public String allActiveCheckDefinitions(Optional<String> token, String query) {
		return "";
	}

	@Override
	public String allActiveCheckDefinitionsLastModified(Optional<String> token, String query) throws URISyntaxException, IOException {
		return null;
	}

	@Override
	public String allActiveAlertDefinitionsLastModified(Optional<String> token, String query) throws URISyntaxException, IOException {
		return null;
	}
}
