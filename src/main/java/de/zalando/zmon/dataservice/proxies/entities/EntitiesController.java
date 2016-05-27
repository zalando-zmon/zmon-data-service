package de.zalando.zmon.dataservice.proxies.entities;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
public class EntitiesController {

    private final EntitiesService entitiesService;

    @Autowired
    public EntitiesController(EntitiesService entitiesService) {
        this.entitiesService = entitiesService;
    }

    @RequestMapping(value = "/api/v1/entities", method = RequestMethod.PUT)
    public String addEntities(@RequestBody(required = true) final String node) throws IOException, URISyntaxException {
        return this.entitiesService.addEntities(node);
    }

    @RequestMapping(value = "/api/v1/entities/{id}", method = RequestMethod.DELETE)
    public String deleteEntity(@PathVariable(value = "id") String id) throws IOException, URISyntaxException {
        return this.entitiesService.deleteEntity(id);
    }

    @RequestMapping(value = "/api/v1/entities")
    public String getEntities(@RequestParam(value = "query", defaultValue = "{}") final String query)
            throws IOException, URISyntaxException {
        return this.entitiesService.getEntities(query);
    }

    @RequestMapping(value = "/rest/api/v1/entities")
    public String getEntitiesControllerEP(@RequestParam(value = "query", defaultValue = "{}") final String query)
            throws IOException, URISyntaxException {
        return this.entitiesService.getEntities(query);
    }

}
