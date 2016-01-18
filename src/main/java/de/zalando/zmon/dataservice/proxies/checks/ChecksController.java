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

//        if(!config.isProxy_controller()) {
//            writer.write("");
//            return;
//        }
//
//        URI uri = new URIBuilder().setPath(config.getProxy_controller_url() + "/checks/all-active-check-definitions").setParameter("query",query).build();
//        final String r = Request.Get(uri).useExpectContinue().execute().returnContent().asString();
//        writer.write(r);
    }

    //TODO, this starts with 'rest'
    @RequestMapping(value = "/rest/api/v1/checks/all-active-check-definitions")
    public String getChecksControllerEP(@RequestParam(value = "query", defaultValue = "{}") final String query) throws IOException, URISyntaxException {
    	return checkService.allActiveCheckDefinitions(query);
//        if(!config.isProxy_controller()) {
//            writer.write("");
//            return;
//        }
//
//        URI uri = new URIBuilder().setPath(config.getProxy_controller_url() + "/checks/all-active-check-definitions").setParameter("query",query).build();
//        final String r = Request.Get(uri).useExpectContinue().execute().returnContent().asString();
//        writer.write(r);
    }

    @RequestMapping(value = "/api/v1/alerts")
    public String getAlerts(@RequestParam(value = "query", defaultValue = "{}") final String query) throws IOException, URISyntaxException {
    	return checkService.allActiveAlertDefinitions(query);
//        if(!config.isProxy_controller()) {
//            writer.write("");
//            return;
//        }
//        URI uri = new URIBuilder().setPath(config.getProxy_controller_url() + "/checks/all-active-alert-definitions").setParameter("query",query).build();
//        final String r = Request.Get(uri).useExpectContinue().execute().returnContent().asString();
//        writer.write(r);
    }

  //TODO, this starts with 'rest'
    @RequestMapping(value = "/rest/api/v1/checks/all-active-alert-definitions")
    public String getAlertsControllerEP(@RequestParam(value = "query", defaultValue = "{}") final String query) throws IOException, URISyntaxException {
    	return checkService.allActiveAlertDefinitions(query);
//        if(!config.isProxy_controller()) {
//            writer.write("");
//            return;
//        }
//        URI uri = new URIBuilder().setPath(config.getProxy_controller_url() + "/checks/all-active-alert-definitions").setParameter("query",query).build();
//        final String r = Request.Get(uri).useExpectContinue().execute().returnContent().asString();
//        writer.write(r);
    }
}
