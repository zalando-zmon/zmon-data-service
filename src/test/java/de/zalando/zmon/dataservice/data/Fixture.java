package de.zalando.zmon.dataservice.data;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

class Fixture {

    private static final Logger LOG = LoggerFactory.getLogger(Fixture.class);

    static WriteData writeData(Optional<WorkerResult> workerResultOptional) {
        return new WriteData(workerResultOptional, "stups", Optional.empty(), 13, "{}");
    }

    public static WorkerResult buildWorkerResult() {
        return doBuildWorkerResult();
    }

    private static WorkerResult doBuildWorkerResult() {
        WorkerResult wr = new WorkerResult();
        wr.account = "0987654321";
        wr.team = "stups";
        wr.results = buildCheckDataList();
        return wr;
    }

    private static List<CheckData> buildCheckDataList() {
        List<CheckData> result = Lists.newArrayList();
        for (int i = 0; i < 5; i++) {
            CheckData cd = new CheckData();
            cd.check_id = i;
            cd.entity_id = "ENTITY_" + i;
            cd.exception = i % 2 == 0;
            cd.run_time = i * 1.2;
            cd.time = "1234567";
            cd.worker = "worker_" + i;
            cd.alerts = buildAlertDataMap();
            cd.check_result = buildCheckResult();
            cd.entity = buildEntityMap();
            result.add(cd);
        }

        return result;
    }

    private static Map<String, String> buildEntityMap() {
        Map<String, String> result = Maps.newHashMap();
        for (int i = 0; i < 3; i++) {
            result.put("entity_" + i, "entityValue_" + i);
        }
        return result;
    }

    private static JsonNode buildCheckResult() {
        return buildJsonNodeFromResource("checkResultNode.json");
    }

    private static Map<String, AlertData> buildAlertDataMap() {
        Map<String, AlertData> result = Maps.newHashMap();
        for (int i = 0; i < 3; i++) {
            AlertData ad = new AlertData();
            ad.active = i % 2 == 0;
            ad.alert_id = i;
            ad.captures = buildCaptures();
            ad.changed = i % 2 == 0;
            ad.downtimes = buildDownTimes();
            ad.exception = i % 2 == 0;
            ad.in_period = i % 2 == 0;
            ad.start_time = "djadfjaadf";
            ad.start_time_ts = 1.2;
            result.put("ad_" + i, ad);
        }
        return result;
    }

    private static JsonNode buildDownTimes() {
        return buildJsonNodeFromResource("downtimes.json");
    }

    private static JsonNode buildCaptures() {
        return buildJsonNodeFromResource("buildCaptures.json");
    }

    static ObjectMapper mapper = new ObjectMapper();

    private static JsonNode buildJsonNodeFromResource(String resource) {
        JsonNode result = null;
        try {
            result = mapper.readTree(Fixture.class.getResourceAsStream(resource));
        } catch (JsonProcessingException e) {
            LOG.warn("Error in : {}", resource);
        } catch (IOException e) {
            LOG.warn("Error in : {}", resource);
        }
        return result;
    }
}
