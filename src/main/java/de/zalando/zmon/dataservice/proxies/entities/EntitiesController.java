package de.zalando.zmon.dataservice.proxies.entities;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import de.zalando.zmon.dataservice.data.DataServiceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
public class EntitiesController {

    private final EntitiesService entitiesService;

    @Autowired
    public EntitiesController(EntitiesService entitiesService) {
        this.entitiesService = entitiesService;
    }

    @RequestMapping(value = "/api/v1/entities", method = RequestMethod.PUT)
    public String addEntities(@RequestBody(required = true) final String node, @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) throws IOException, URISyntaxException {
        Optional<String> token = DataServiceController.extractTokenFromHeader(authHeader);
        return this.entitiesService.addEntities(token, node);
    }

    @RequestMapping(value = "/api/v1/entities/{id}", method = RequestMethod.DELETE)
    public String deleteEntity(@PathVariable(value = "id") String id, @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) throws IOException, URISyntaxException {
        Optional<String> token = DataServiceController.extractTokenFromHeader(authHeader);
        return this.entitiesService.deleteEntity(token, id);
    }

    @RequestMapping(value = "/api/v1/entities")
    public String getEntities(@RequestParam(value = "query", defaultValue = "{}") final String query, @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader)
            throws IOException, URISyntaxException {
        Optional<String> token = DataServiceController.extractTokenFromHeader(authHeader);
        return this.entitiesService.getEntities(token, query);
    }

    @RequestMapping(value = "/rest/api/v1/entities")
    public String getEntitiesControllerEP(@RequestParam(value = "query", defaultValue = "{}") final String query, @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader)
            throws IOException, URISyntaxException {
        Optional<String> token = DataServiceController.extractTokenFromHeader(authHeader);
        return this.entitiesService.getEntities(token, query);
    }

}
