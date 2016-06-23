package de.zalando.zmon.dataservice.proxies.checks;

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

}
