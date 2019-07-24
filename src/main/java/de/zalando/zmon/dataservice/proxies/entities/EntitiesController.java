package de.zalando.zmon.dataservice.proxies.entities;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.net.URISyntaxException;

import static de.zalando.zmon.dataservice.oauth2.BearerToken.extractFromHeader;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
public class EntitiesController {

    private final EntitiesService entitiesService;

    @Autowired
    public EntitiesController(EntitiesService entitiesService) {
        this.entitiesService = entitiesService;
    }

    @RequestMapping(value = "/api/v1/entities", method = RequestMethod.PUT)
    public String addEntities(@RequestBody(required = true) final String node, @RequestHeader(AUTHORIZATION) String authHeader) throws IOException, URISyntaxException {
        return entitiesService.addEntities(extractFromHeader(authHeader), node);
    }

    @RequestMapping(value = "/api/v1/entities/{id}", method = RequestMethod.DELETE)
    public String deleteEntity(@PathVariable(value = "id") String id, @RequestHeader(AUTHORIZATION) String authHeader) throws IOException, URISyntaxException {
        return entitiesService.deleteEntity(extractFromHeader(authHeader), id);
    }

    @RequestMapping(value = {"/api/v1/entities"})
    public String getEntities(@RequestParam(value = "query", defaultValue = "{}") final String query, @RequestHeader(AUTHORIZATION) String authHeader, @RequestParam(value="exclude", defaultValue="") final String exclude)
            throws IOException, URISyntaxException {
        return entitiesService.getEntities(extractFromHeader(authHeader), query, exclude);
    }
}
