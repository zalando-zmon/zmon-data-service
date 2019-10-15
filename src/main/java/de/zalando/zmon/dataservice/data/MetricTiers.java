package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import de.zalando.zmon.dataservice.TokenWrapper;
import de.zalando.zmon.dataservice.components.DefaultObjectMapper;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

import static com.google.common.collect.Sets.newHashSet;

@Component
public class MetricTiers {
    public static final Logger LOG = LoggerFactory.getLogger(MetricTiers.class);

    private final ObjectMapper objectMapper;
    private final Executor executor;
    private final TokenWrapper wrapper;
    private final String hostname;

    private Integer ingestMaxCheckTier;
    private Set<Integer> criticalChecks;
    private Set<Integer> importantChecks;

    public MetricTiers(@DefaultObjectMapper ObjectMapper objectMapper,
                       @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") DataServiceConfigProperties config,
                       @Qualifier("accessTokens") TokenWrapper wrapper) {
        this.objectMapper = objectMapper;
        this.hostname = config.getProxyControllerBaseUrl();
        this.wrapper = wrapper;
        this.executor = HttpClientFactory.getExecutor(
                config.getRestMetricSocketTimeout(),
                config.getRestMetricTimeout(),
                config.getRestMetricConnections(),
                config.getConnectionsTimeToLive()
        );

        ingestMaxCheckTier = 0;
        criticalChecks = newHashSet();
        importantChecks = newHashSet();
    }

    @Scheduled(fixedRate = 60_000)
    public void updateTiers() {
        LOG.debug("Updating metric tiers configuration");
        try {
            this.ingestMaxCheckTier = getMaxCheckTier();

            final Map<String, Set<Integer>> checkTiers = getCheckTiers();
            this.criticalChecks = checkTiers.get("critical");
            this.importantChecks = checkTiers.get("important");
        } catch (IOException e) {
            LOG.error("Metric tiers configuration update failed", e);
        }
    }

    public boolean isMetricEnabled(int checkId) {
        if (ingestMaxCheckTier <= 0 || ingestMaxCheckTier >= 3) return true;
        else if (ingestMaxCheckTier == 1) return criticalChecks.contains(checkId);
        else return criticalChecks.contains(checkId) || importantChecks.contains(checkId);
    }

    public boolean isMetricDisabled(int checkId) {
        return !isMetricEnabled(checkId);
    }

    private Integer getMaxCheckTier() throws IOException {
        final Optional<JsonNode> data = getEntityData("zmon-service-level-config");
        return data.map(d -> d.get("ingest_max_check_tier")).map(JsonNode::asInt).orElse(0);
    }

    private Map<String, Set<Integer>> getCheckTiers() throws IOException {
        final Optional<JsonNode> data = getEntityData("zmon-check-tiers");

        final Set<Integer> criticalChecks = getCheckList(data, "critical");
        final Set<Integer> importantChecks = getCheckList(data, "important");

        return ImmutableMap.of("critical", criticalChecks, "important", importantChecks);
    }

    private Optional<JsonNode> getEntityData(final String entityId) throws IOException {
        final String uri = hostname + "/api/v1/entities/" + entityId;
        final Request request = Request.Get(uri).addHeader("Authorization", "Bearer " + wrapper.get());
        final String response = executor.execute(request).returnContent().toString();
        final JsonNode jsonNode = objectMapper.readTree(response);
        return Optional.ofNullable(jsonNode.get("data"));
    }

    private Set<Integer> getCheckList(final Optional<JsonNode> data, final String kind) {
        final Iterator<JsonNode> checks = data.map(d -> d.get(kind)).map(JsonNode::iterator).orElse(Collections.emptyIterator());
        final Set<Integer> checkSet = new HashSet<>();
        while (checks.hasNext()) {
            checkSet.add(checks.next().asInt());
        }
        return checkSet;
    }

    void setIngestMaxCheckTier(Integer ingestMaxCheckTier) {
        this.ingestMaxCheckTier = ingestMaxCheckTier;
    }

    void setCriticalChecks(Set<Integer> criticalChecks) {
        this.criticalChecks = criticalChecks;
    }

    void setImportantChecks(Set<Integer> importantChecks) {
        this.importantChecks = importantChecks;
    }
}
