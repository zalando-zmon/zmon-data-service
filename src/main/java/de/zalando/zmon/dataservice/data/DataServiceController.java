package de.zalando.zmon.dataservice.data;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.components.CustomObjectMapper;
import de.zalando.zmon.dataservice.components.DefaultObjectMapper;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

@RestController
@RequestMapping("/api/v1/data")
public class DataServiceController {
	
	private final Logger log = LoggerFactory.getLogger(DataServiceController.class);

	private final DataServiceMetrics metrics;

	private final KairosDBStore kairosStore;

	private final AppMetricsClient applicationMetricsClient;

	private final RedisDataStore storage;

	private final DataServiceConfigProperties config;

	private final ObjectMapper mapper;

	private final ObjectMapper valueMapper;

	@Autowired
	public DataServiceController(DataServiceConfigProperties config, AppMetricsClient applicationMetricsClient,
			RedisDataStore storage, KairosDBStore kairosStore, DataServiceMetrics dataServiceMetrics, @DefaultObjectMapper ObjectMapper defaultObjectMapper, @CustomObjectMapper ObjectMapper customObjectMapper) {
		this.config = config;
		this.applicationMetricsClient = applicationMetricsClient;
		this.storage = storage;
		this.kairosStore = kairosStore;
		this.metrics = dataServiceMetrics;
		this.mapper = defaultObjectMapper;
		this.valueMapper = customObjectMapper;
	}

	@RequestMapping(value = "/trial-run/", method = RequestMethod.PUT, consumes = { "text/plain",
			"application/json" })
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

	@RequestMapping(value = "/{account}/{checkid}/", method = RequestMethod.PUT, consumes = { "text/plain",
			"application/json" })
	void putData(@PathVariable(value = "checkid") int checkId, @PathVariable(value = "account") String accountId,
			@RequestBody String data) {

		metrics.markAccount(accountId, data.length());
		metrics.markCheck(checkId, data.length());

		if (config.isLogCheckData()) {
			log.info("{}", data);
		}

		Optional<WorkerResult> wrOptional = extractAndFilter(data, accountId, checkId);
		if(!wrOptional.isPresent()){
			return;
		}

		// TODO, can these writes be done parallel?
		writeRedis(wrOptional.get(), data, checkId);

		writeApplicationMetrics(wrOptional.get(), data);

		writeKairos(wrOptional.get(), data, checkId);
	}
	
	protected Optional<WorkerResult> extractAndFilter(String data, String accountId, int checkId){
		Optional<WorkerResult> wrOptional = Optional.empty();
		try {
			wrOptional = Optional.ofNullable(valueMapper.readValue(data, new TypeReference<WorkerResult>() {
			}));
			if(wrOptional.isPresent()){
				
				metrics.markRate(wrOptional.get().results.size());
				
				// make sure that the unique account it is actually in th
				// aws:<accountid> string
				// this should protect us from wrongly configured schedulers that
				// execute the wrong checks
				wrOptional.get().results = wrOptional.get().results.stream().filter(x -> x.entity_id.contains(accountId)).collect(Collectors.toList());
			}
			return wrOptional;
		} catch (Exception e) {
			log.error("failed parse for check={} data={}", checkId, data, e);
			metrics.markParseError();
			return wrOptional;
		}
	}
	
	protected void writeRedis(WorkerResult wr, String data, int checkId){
		try {
			storage.store(wr);
		} catch (Exception e) {
			log.error("failed redis write check={} data={}", checkId, data, e);
			metrics.markRedisError();
		}
	}
	
	protected void writeApplicationMetrics(WorkerResult wr, String data){
		try {
			Map<Integer, List<CheckData>> partitions = wr.results.stream()
					.filter(x -> config.getActuatorMetricChecks().contains(x.check_id)).filter(x -> !x.exception)
					.collect(Collectors.groupingBy(x -> Math
							.abs(x.entity.get("application_id").hashCode() % config.getRestMetricHosts().size())));

			applicationMetricsClient.receiveData(partitions);
		} catch (Exception ex) {
			log.error("Failed to write to REST metrics data={}", data, ex);
		}
	}
	
	protected void writeKairos(WorkerResult wr, String data, int checkId) {
		Timer.Context c = metrics.getKairosDBTimer().time();
		try {
			kairosStore.store(wr);
		} catch (Exception e) {
			log.error("failed kairosdb write check={} data={}", checkId, data, e);
			metrics.markKairosError();
		} finally {
			c.stop();
		}
	}
}
