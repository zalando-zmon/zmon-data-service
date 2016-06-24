package de.zalando.zmon.dataservice.proxies.checks;

import de.zalando.zmon.dataservice.oauth2.BearerToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URISyntaxException;

@RestController
public class ChecksController {

    private final ChecksService checkService;

    @Autowired
    public ChecksController(ChecksService checksService) {
        this.checkService = checksService;
    }

    // TODO: remove legacy "/rest" prefix
    @RequestMapping(value = {"/api/v1/checks/all-active-check-definitions", "/rest/api/v1/checks/all-active-check-definitions"})
    public String getChecksControllerEP(@RequestParam(value = "query", defaultValue = "{}") final String query,
                                        HttpServletRequest request) throws IOException, URISyntaxException {
        return checkService.allActiveCheckDefinitions(BearerToken.extract(request), query);
    }

    // TODO: remove legacy "/rest" prefix
    @RequestMapping(value = {"/api/v1/checks/all-active-alert-definitions", "/rest/api/v1/checks/all-active-alert-definitions"})
    public String getAlertsControllerEP(@RequestParam(value = "query", defaultValue = "{}") final String query,
                                        HttpServletRequest request) throws IOException, URISyntaxException {
        return checkService.allActiveAlertDefinitions(BearerToken.extract(request), query);
    }
}
