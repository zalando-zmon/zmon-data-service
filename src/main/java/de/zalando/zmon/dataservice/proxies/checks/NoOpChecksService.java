package de.zalando.zmon.dataservice.proxies.checks;

public class NoOpChecksService implements ChecksService {

	@Override
	public String allActiveAlertDefinitions(String query) {
		return "";
	}

	@Override
	public String allActiveCheckDefinitions(String query) {
		return "";
	}

}
