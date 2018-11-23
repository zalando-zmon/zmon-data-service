package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.zalando.zmon.dataservice.TokenWrapper;
import de.zalando.zmon.dataservice.components.DefaultObjectMapper;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class WhitelistedChecks {
    private static final Logger LOG = LoggerFactory.getLogger(WhitelistedChecks.class);

    private final ObjectMapper objectMapper;
    private final Executor executor;
    private final TokenWrapper wrapper;
    private final String hostname;
    private List<Integer> whitelist;

    @Autowired
    public WhitelistedChecks(@DefaultObjectMapper ObjectMapper objectMapper,
                             DataServiceConfigProperties config,
                             @Qualifier("accessTokens") TokenWrapper wrapper) {
        this.objectMapper = objectMapper;
        this.executor = HttpClientFactory.getExecutor(
                config.getRestMetricSocketTimeout(),
                config.getRestMetricTimeout(),
                config.getRestMetricConnections(),
                config.getConnectionsTimeToLive()

        );

        this.hostname = config.getProxyControllerUrl();
        this.wrapper = wrapper;
        this.whitelist = new ArrayList<>();
    }

    @Scheduled(fixedRate = 60_000)
    public void updateWhitelist() {
        try {
            LOG.debug("started updating whitelist. Old whitelist size={}",whitelist.size());
            Request request = Request.Get(hostname + "/api/v1/entities/zmon-checkid-whitelist")
                    .addHeader("Authorization", "Bearer " + wrapper.get());
            String data = executor.execute(request).returnContent().toString();
            JsonNode jsonNode = objectMapper.readTree(data);
            Stream<Integer> checkIdsStream = jsonNode.findValues("check_ids").stream().map(JsonNode::intValue);
            this.whitelist = checkIdsStream.collect(Collectors.toList());
            LOG.debug("whitelist updated. New whitelist size={}",whitelist.size());
        } catch (Exception e) {
            LOG.error("error updating whitelist", e);
        }
    }

    public List<Integer> getWhitelist() {
        return whitelist;
    }
}
