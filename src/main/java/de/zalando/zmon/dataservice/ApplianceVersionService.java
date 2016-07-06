package de.zalando.zmon.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.zalando.zmon.dataservice.proxies.entities.EntitiesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * Created by jmussler on 05.07.16.
 */
@Component
public class ApplianceVersionService {

    private final EntitiesService entityService;
    private final ObjectMapper mapper = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(ApplianceVersionService.class);

    @Autowired
    public ApplianceVersionService(EntitiesService entityService) {
        this.entityService = entityService;
    }

    public JsonNode getVersionConfig(Optional<String> token) {
        if (!token.isPresent()) {
            return null;
        }

        try {
            String result = entityService.getEntities(token, "[{\"id\":\"zmon-appliance-config\"}]");
            JsonNode node = mapper.readTree(result);
            if (node.size() != 1) {
                return null;
            }

            if (!node.get(0).has("data") || !node.get(0).get("data").has("version-config")) {
                return null;
            }

            return node.get(0).get("data").get("version-config");
        } catch (IOException | URISyntaxException ex) {
            logger.error("Failed to get config entity: {}", ex.getMessage());
        }
        return null;
    }
}
