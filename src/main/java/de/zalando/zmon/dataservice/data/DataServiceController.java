package de.zalando.zmon.dataservice.data;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import de.zalando.zmon.dataservice.ApplianceVersionService;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import de.zalando.zmon.dataservice.oauth2.BearerToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.components.CustomObjectMapper;
import de.zalando.zmon.dataservice.components.DefaultObjectMapper;

@RestController
@RequestMapping("/api")
public class DataServiceController {

    private final Logger log = LoggerFactory.getLogger(DataServiceController.class);

    private final DataServiceMetrics metrics;

    private final RedisDataStore storage;

    private final ObjectMapper mapper;

    private final ObjectMapper valueMapper;

    private final List<WorkResultWriter> workResultWriter;

    private final ProxyWriter proxyWriter;

    private final DataServiceConfigProperties config;

    private final ApplianceVersionService applianceVersionService;

    @Autowired
    public DataServiceController(RedisDataStore storage, DataServiceMetrics dataServiceMetrics,
                                 @DefaultObjectMapper ObjectMapper defaultObjectMapper, @CustomObjectMapper ObjectMapper customObjectMapper,
                                 List<WorkResultWriter> workResultWriter, ProxyWriter proxyWriter, DataServiceConfigProperties config, ApplianceVersionService applianceVersionService) {
        this.storage = storage;
        this.metrics = dataServiceMetrics;
        this.mapper = defaultObjectMapper;
        this.valueMapper = customObjectMapper;
        this.workResultWriter = workResultWriter;
        this.proxyWriter = proxyWriter;
        this.applianceVersionService = applianceVersionService;
        this.config = config;
    }

    @RequestMapping(value = "/v1/appliance-versions", method = RequestMethod.GET)
    public ResponseEntity<JsonNode> getVersionConfig(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        JsonNode node = applianceVersionService.getVersionConfig(BearerToken.extractFromHeader(authHeader));
        if (null == node) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(node, HttpStatus.OK);
    }

    @RequestMapping(value = "/v1/data/trial-run/", method = RequestMethod.PUT, consumes = {"text/plain", "application/json"})
    void putTrialRunData(@RequestBody String data) {
        try {
            metrics.markTrialRunData();
            JsonNode node = mapper.readTree(data);
            storage.storeTrialRun(node.get("id").textValue(), node.get("result").get("entity").get("id").textValue(),
                    mapper.writeValueAsString(node.get("result")));
        } catch (Exception ex) {
            log.error("", ex);
            metrics.markTrialRunError();
        }
    }

    @RequestMapping(value = {"/v1/data/{account}/{checkid}/", "/v1/data/{account}/{checkid}"}, method = RequestMethod.PUT, consumes = {"text/plain",
            "application/json"})
    void putData(@PathVariable(value = "checkid") int checkId, @PathVariable(value = "account") String accountId,
                 @RequestBody String data, @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        proxyData(authHeader, accountId, String.valueOf(checkId), data);

        Optional<WorkerResult> wrOptional = extractAndFilter(data, accountId, checkId);
        WriteData writeData = new WriteData(wrOptional, accountId, Optional.empty(), checkId, data);

        // some writer are async, keep in mind
        workResultWriter.forEach(writer -> writer.write(writeData));
    }

    protected void proxyData(String authHeader, String accountId, String checkId, String data) {
        BearerToken.extractFromHeader(authHeader).ifPresent(t -> proxyWriter.write(t, accountId, checkId, data));
    }

    protected Optional<WorkerResult> extractAndFilter(String data, String accountId, int checkId) {
        Optional<WorkerResult> wrOptional = Optional.empty();
        try {
            wrOptional = Optional.ofNullable(valueMapper.readValue(data, new TypeReference<WorkerResult>() {
            }));
            if (wrOptional.isPresent()) {

                metrics.markRate(wrOptional.get().results.size());

                // arbitrary entities might be pushed from on-premise data centers "dc:..."
                if (!accountId.startsWith("dc:")) {
                    // make sure that the unique account it is actually in th
                    // aws:<accountid> string
                    // this should protect us from wrongly configured schedulers
                    // that
                    // execute the wrong checks
                    wrOptional.get().results = wrOptional.get().results.stream()
                            .filter(x -> x.entity_id.contains(accountId)).collect(Collectors.toList());
                }
            }
            return wrOptional;
        } catch (Exception e) {
            log.error("failed parse for check={} data={}", checkId, data, e);
            metrics.markParseError();
            return wrOptional;
        }
    }

    @RequestMapping(value = {"/v2/data/{account}/{checkid}/{region}/", "/v2/data/{account}/{checkid}/{region}"}, method = RequestMethod.PUT, consumes = {"text/plain",
            "application/json"})
    void putData(@PathVariable(value = "checkid") int checkId, @PathVariable(value = "account") String accountId, @PathVariable(value = "region") String region,
                 @RequestBody String data, @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        proxyData(authHeader, accountId, String.valueOf(checkId), data);

        Optional<WorkerResult> wrOptional = extractAndFilter(data, accountId, checkId);
        WriteData writeData = new WriteData(wrOptional, accountId, Optional.of(region), checkId, data);

        // some writer are async, keep in mind
        workResultWriter.forEach(writer -> writer.write(writeData));
    }
}
