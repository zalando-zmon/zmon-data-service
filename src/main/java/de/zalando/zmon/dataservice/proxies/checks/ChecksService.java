package de.zalando.zmon.dataservice.proxies.checks;

import java.io.IOException;
import java.net.URISyntaxException;

public interface ChecksService {

	String allActiveAlertDefinitions(String query) throws URISyntaxException, IOException;

	String allActiveCheckDefinitions(String query) throws URISyntaxException, IOException;

}
