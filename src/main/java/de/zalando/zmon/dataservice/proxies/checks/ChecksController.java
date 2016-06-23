package de.zalando.zmon.dataservice.proxies.checks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;

import static de.zalando.zmon.dataservice.oauth2.BearerToken.extractFromHeader;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

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
                                        @RequestHeader(AUTHORIZATION) String authHeader) throws IOException, URISyntaxException {
        return checkService.allActiveCheckDefinitions(extractFromHeader(authHeader), query);
    }

    // TODO: remove legacy "/rest" prefix
    @RequestMapping(value = {"/api/v1/checks/all-active-alert-definitions", "/rest/api/v1/checks/all-active-alert-definitions"})
    public String getAlertsControllerEP(@RequestParam(value = "query", defaultValue = "{}") final String query,
                                        @RequestHeader(AUTHORIZATION) String authHeader) throws IOException, URISyntaxException {
        return checkService.allActiveAlertDefinitions(extractFromHeader(authHeader), query);
    }
}
