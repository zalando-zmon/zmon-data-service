package de.zalando.zmon.dataservice.proxies.mobile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import de.zalando.zmon.dataservice.oauth2.BearerToken;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import de.zalando.zmon.dataservice.components.DefaultObjectMapper;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jmussler on 12/16/15.
 */

@RestController
@RequestMapping(value = "/api/v1/mobile/", produces = {MediaType.APPLICATION_JSON_VALUE})
public class MobileApiProxy {

    private final DataServiceConfigProperties config;

    private final ObjectMapper mapper;

    @Autowired
    public MobileApiProxy(DataServiceConfigProperties config, @DefaultObjectMapper ObjectMapper defaultObjectMapper) {
        Assert.notNull(config, "'config' should never be null");
        Assert.notNull(defaultObjectMapper, "'defaultObjectMapper' should never be null");
        this.config = config;
        this.mapper = defaultObjectMapper;
    }

    public static class AlertHeader {
        public String name;
        public int id;
        public String team;
        public String responsible_team;
    }

    @RequestMapping(value = "alert", method = RequestMethod.GET)
    public ResponseEntity<List<AlertHeader>> getAllAlerts(@RequestParam(value = "team", required = false, defaultValue = "*") String team, HttpServletRequest request) throws URISyntaxException, IOException {

        Optional<String> token = BearerToken.extract(request);

        URI uri = new URIBuilder().setPath(config.getProxyControllerBaseUrl() + "/api/v1/checks/all-active-alert-definitions").build();
        Request proxyRequest = Request.Get(uri);
        if (token.isPresent()) {
            proxyRequest.addHeader("Authorization", "Bearer " + token.get());
        }

        final String r = proxyRequest.execute().returnContent().asString();
        JsonNode node = mapper.readTree(r);
        List<AlertHeader> alerts = new ArrayList<>();
        JsonNode alertDefinitions = node.get("alert_definitions");
        if (alertDefinitions != null) {
            Iterator<JsonNode> i = alertDefinitions.iterator();
            while (i.hasNext()) {
                AlertHeader h = new AlertHeader();
                JsonNode n = i.next();
                if (!team.equals("*") && !n.get("team").textValue().startsWith(team)) {
                    continue;
                }

                h.id = n.get("id").asInt();
                h.name = n.get("name").textValue();
                h.team = n.get("team").textValue();
                h.responsible_team = n.get("responsible_team").textValue();
                alerts.add(h);
            }
        }

        return new ResponseEntity<>(alerts, HttpStatus.OK);
    }

    @RequestMapping(value = "alert/{alert_id}", method = RequestMethod.GET)
    public ResponseEntity<String> getAlertDetails(@PathVariable(value = "alert_id") int alertId, HttpServletRequest request) throws URISyntaxException, IOException {
        URI uri = new URIBuilder().setPath(config.getProxyControllerBaseUrl() + "/api/v1/status/alert/" + alertId + "/details").build();

        Optional<String> token = BearerToken.extract(request);

        Request proxyRequest = Request.Get(uri);
        if (token.isPresent()) {
            proxyRequest.addHeader("Authorization", "Bearer " + token.get());
        }

        final String r = proxyRequest.execute().returnContent().asString();
        return new ResponseEntity<>(r, HttpStatus.OK);
    }

    @RequestMapping(value = "active-alerts", method = RequestMethod.GET)
    public ResponseEntity<String> getActiveAlerts(@RequestParam(value = "team", required = false, defaultValue = "*") String team, HttpServletRequest request) throws URISyntaxException, IOException {

        URI uri = new URIBuilder().setPath(config.getProxyControllerBaseUrl() + "/api/v1/status/active-alerts").addParameter("team", team).build();

        Optional<String> token = BearerToken.extract(request);

        Request proxyRequest = Request.Get(uri);
        if (token.isPresent()) {
            proxyRequest.addHeader("Authorization", "Bearer " + token.get());
        }

        final String r = proxyRequest.execute().returnContent().asString();
        return new ResponseEntity<>(r, HttpStatus.OK);
    }

    @RequestMapping(value = "status", method = RequestMethod.GET)
    public ResponseEntity<String> getZMONStatus(HttpServletRequest request) throws URISyntaxException, IOException {

        URI uri = new URIBuilder().setPath(config.getProxyControllerBaseUrl() + "/api/v1/status").build();

        Optional<String> token = BearerToken.extract(request);

        Request proxyRequest = Request.Get(uri);
        if (token.isPresent()) {
            proxyRequest.addHeader("Authorization", "Bearer " + token.get());
        }

        final String r = proxyRequest.execute().returnContent().asString();
        return new ResponseEntity<>(r, HttpStatus.OK);
    }

    @RequestMapping(value = "all-teams", method = RequestMethod.GET)
    public ResponseEntity<String> getAllTeams(HttpServletRequest request) throws URISyntaxException, IOException {

        URI uri = new URIBuilder().setPath(config.getProxyControllerBaseUrl() + "/api/v1/teams").build();

        Optional<String> token = BearerToken.extract(request);

        Request proxyRequest = Request.Get(uri);
        if (token.isPresent()) {
            proxyRequest.addHeader("Authorization", "Bearer " + token.get());
        }

        final String r = proxyRequest.execute().returnContent().asString();
        return new ResponseEntity<>(r, HttpStatus.OK);
    }
}
