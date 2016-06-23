package de.zalando.zmon.dataservice.proxies.checks;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChecksController {
	
	private final ChecksService checkService;

	@Autowired
	public ChecksController(ChecksService checksService) {
		this.checkService = checksService;
	}


    @RequestMapping(value = "/api/v1/checks")
    public String getChecks(@RequestParam(value = "query", defaultValue = "{}") final String query) throws IOException, URISyntaxException {
    	return checkService.allActiveCheckDefinitions(query);
    }

    // TODO: remove legacy "/rest" prefix
    @RequestMapping(value = {"/api/v1/checks/all-active-check-definitions", "/rest/api/v1/checks/all-active-check-definitions"})
    public String getChecksControllerEP(@RequestParam(value = "query", defaultValue = "{}") final String query) throws IOException, URISyntaxException {
    	return checkService.allActiveCheckDefinitions(query);
    }

    @RequestMapping(value = "/api/v1/alerts")
    public String getAlerts(@RequestParam(value = "query", defaultValue = "{}") final String query) throws IOException, URISyntaxException {
    	return checkService.allActiveAlertDefinitions(query);
    }

    // TODO: remove legacy "/rest" prefix
    @RequestMapping(value = {"/api/v1/checks/all-active-alert-definitions", "/rest/api/v1/checks/all-active-alert-definitions"})
    public String getAlertsControllerEP(@RequestParam(value = "query", defaultValue = "{}") final String query) throws IOException, URISyntaxException {
    	return checkService.allActiveAlertDefinitions(query);
    }
}
