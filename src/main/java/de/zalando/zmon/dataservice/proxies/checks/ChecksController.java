package de.zalando.zmon.dataservice.proxies.checks;

import de.zalando.zmon.dataservice.oauth2.BearerToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;

@RestController
public class ChecksController {

    private final ChecksService checkService;

    @Autowired
    public ChecksController(ChecksService checksService) {
        this.checkService = checksService;
    }

    @RequestMapping(value = {"/api/v1/checks/all-active-check-definitions"}, method=RequestMethod.HEAD)
    public void getChecksControllerEPLastModified(@RequestParam(value = "query", defaultValue = "{}") final String query,
                                                  HttpServletRequest request,HttpServletResponse response) throws IOException, URISyntaxException {
        response.setHeader("Last-Modified", checkService.allActiveCheckDefinitionsLastModified(BearerToken.extract(request), query));
    }

    @RequestMapping(value = {"/api/v1/checks/all-active-check-definitions"})
    public String getChecksControllerEP(@RequestParam(value = "query", defaultValue = "{}") final String query,
                                        HttpServletRequest request) throws IOException, URISyntaxException {
        return checkService.allActiveCheckDefinitions(BearerToken.extract(request), query);
    }

    @RequestMapping(value = {"/api/v1/checks/all-active-alert-definitions"}, method=RequestMethod.HEAD)
    public void getAlertsControllerEPLastModified(@RequestParam(value = "query", defaultValue = "{}") final String query,
                                                  HttpServletRequest request, HttpServletResponse response) throws IOException, URISyntaxException {
        response.setHeader("Last-Modified", checkService.allActiveAlertDefinitionsLastModified(BearerToken.extract(request), query));
    }

    @RequestMapping(value = {"/api/v1/checks/all-active-alert-definitions"})
    public String getAlertsControllerEP(@RequestParam(value = "query", defaultValue = "{}") final String query,
                                        HttpServletRequest request) throws IOException, URISyntaxException {
        return checkService.allActiveAlertDefinitions(BearerToken.extract(request), query);
    }
}
