package de.zalando.zmon.dataservice;

import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.zalando.zmon.dataservice.restmetrics.AppMetricsService;
import de.zalando.zmon.dataservice.restmetrics.ApplicationVersion;
import de.zalando.zmon.dataservice.restmetrics.VersionResult;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;

/**
 * Created by jmussler on 4/21/15.
 */
@RestController
@EnableAutoConfiguration
@EnableConfigurationProperties
@Configuration
@ComponentScan
public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    @Autowired
    DataServiceMetrics metrics;

    @Autowired
    DataServiceConfig config;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    RedisDataStore storage;

    @Autowired
    KairosDBStore kairosStore;

    @Autowired
    AppMetricsService applicationRestMetrics;

    @Bean
    @Autowired
    JedisPool getPool(DataServiceConfig config) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setTestOnBorrow(true);
        poolConfig.setMaxTotal(config.getRedis_pool_size());

        return new JedisPool(poolConfig, config.redis_host(), config.redis_port());
    }

    private static ObjectMapper valueMapper;
    static {
        valueMapper = new ObjectMapper();
        valueMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        valueMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }


    @ResponseBody
    @RequestMapping(value = "/api/v1/trial-runs/{dc}/", method = RequestMethod.GET, produces = "application/json")
    public void getTrialRuns(final Writer writer, final HttpServletResponse response, @PathVariable(value = "dc") final String dcId) throws IOException, URISyntaxException {
        if(!config.proxy_scheduler()) {
            writer.write("");
            return;
        }

        response.setContentType("application/json");
        URI uri = new URIBuilder().setPath(config.proxy_scheduler_url() + "/trial-runs/"+dcId+"/").build();
        final String r = Request.Get(uri).useExpectContinue().execute().returnContent().asString();
        writer.write(r);
    }

    @ResponseBody
    @RequestMapping(value = "/api/v1/instant-evaluations/{dc}/", method = RequestMethod.GET, produces = "application/json")
    public void getInstantEvals(final Writer writer, final HttpServletResponse response, @PathVariable(value = "dc") final String dcId) throws IOException, URISyntaxException {
        if(!config.proxy_scheduler()) {
            writer.write("");
            return;
        }

        response.setContentType("application/json");
        URI uri = new URIBuilder().setPath(config.proxy_scheduler_url() + "/instant-evaluations/"+dcId+"/").build();
        final String r = Request.Get(uri).useExpectContinue().execute().returnContent().asString();
        writer.write(r);
    }

    @RequestMapping(value="/api/v1/data/trial-run/", method= RequestMethod.PUT, consumes = {"text/plain", "application/json"})
    void putTrialRunData(@RequestBody String data) {
        try {
            metrics.markTrialRunData();
            JsonNode node = mapper.readTree(data);
            storage.storeTrialRun(node.get("id").textValue(), node.get("result").get("entity").get("id").textValue(), mapper.writeValueAsString(node.get("result")));
        }
        catch (Exception ex) {
            LOG.error("", ex);
            metrics.markTrialRunError();
        }
    }

    @RequestMapping(value="/api/v1/rest-api-metrics/", method=RequestMethod.GET)
    public VersionResult getRestApiMetrics(@RequestParam(value="application_id") String applicationId, @RequestParam(value="application_version") String applicationVersion, @RequestParam(value="redirect", defaultValue="False") boolean redirect) {
        if(!redirect) {
            return applicationRestMetrics.getAggrMetrics(applicationId, applicationVersion, System.currentTimeMillis());
        }
        else {
            // resolve list of servers for application ID
            return null;
        }
    }

    @RequestMapping(value="/api/v1/data/{account}/{checkid}/", method= RequestMethod.PUT, consumes = {"text/plain", "application/json"})
    void putData(@PathVariable(value="checkid") int checkId, @PathVariable(value="account") String accountId, @RequestBody String data) {

        metrics.markAccount(accountId, data.length());
        metrics.markCheck(checkId, data.length());

        if(config.log_check_data()) {
            LOG.info("{}", data);
        }

        WorkerResult wr;
        try {
            wr = valueMapper.readValue(data, new TypeReference<WorkerResult>(){});
            metrics.markRate(wr.results.size());

            // make sure that the unique account it is actually in th aws:<accountid> string
            // this should protect us from wrongly configured schedulers that execute the wrong checks
            wr.results = wr.results.stream().filter(x->x.entity_id.contains(accountId)).collect(Collectors.toList());
        }
        catch(Exception e) {
            LOG.error("failed parse for check={} data={}", checkId, data, e);
            metrics.markParseError();
            return;
        }

        try {
            storage.store(wr);
        }
        catch(Exception e) {
            LOG.error("failed redis write check={} data={}", checkId, data, e);
            metrics.markRedisError();
        }

        try {
            for(CheckData d : wr.results) {
                if(config.actuator_metric_checks().contains(d.check_id)) {
                    Double ts = d.check_result.get("ts").asDouble();
                    ts = ts * 1000.;
                    Long tsL = ts.longValue();
                    applicationRestMetrics.pushMetric(d.entity.get("application_id"), d.entity.get("application_version"),d.entity_id, tsL, d.check_result.get("value"));
                }
            }
        }
        catch(Exception ex) {
            LOG.error("Failed to write to REST metrics data={}", data, ex);
        }

        Timer.Context c = metrics.getKairosDBTimer().time();
        try {
            kairosStore.store(wr);
        }
        catch(Exception e) {
            LOG.error("failed kairosdb write check={} data={}", checkId, data, e);
            metrics.markKairosError();
        }
        finally {
            c.stop();
        }
    }

    @ResponseBody
    @RequestMapping(value = "/api/v1/entities/", method = RequestMethod.PUT, produces = "application/json")
    public void getEntities(final Writer writer, final HttpServletResponse response, @RequestBody(required=true) final JsonNode node) throws IOException, URISyntaxException {
        if(!config.proxy_controller()) {
            writer.write("");
            return;
        }

        if(node.has("infrastructure_account")) {
            String id = node.get("infrastructure_account").textValue();
            metrics.markEntity(id, 1);
        }

        response.setContentType("application/json");
        URI uri = new URIBuilder().setPath(config.proxy_controller_url() + "/entities/").build();

        final Executor executor = Executor.newInstance();

        String r = executor.execute(Request.Put(uri).useExpectContinue().bodyString(valueMapper.writeValueAsString(node),
                ContentType.APPLICATION_JSON)).returnContent().asString();

        writer.write(r);
    }

    @ResponseBody
    @RequestMapping(value = "/api/v1/entities/{id}/", method = RequestMethod.DELETE, produces = "application/json")
    public void deleteEntity(final Writer writer, final HttpServletResponse response, @PathVariable(value="id") String id) throws IOException, URISyntaxException {
        if(!config.proxy_controller()) {
            writer.write("");
            return;
        }

        response.setContentType("application/json");
        URI uri = new URIBuilder().setPath(config.proxy_controller_url() + "/entities/"+id+'/').build();

        final Executor executor = Executor.newInstance();

        String r = executor.execute(Request.Delete(uri).useExpectContinue()).returnContent().asString();

        writer.write(r);
    }

    @ResponseBody
    @RequestMapping(value = "/api/v1/entities/", method = RequestMethod.GET, produces = "application/json")
    public void getEntities(final Writer writer, final HttpServletResponse response, @RequestParam(value = "query", defaultValue = "{}") final String query) throws IOException, URISyntaxException {
        if(!config.proxy_controller()) {
            writer.write("");
            return;
        }

        response.setContentType("application/json");
        URI uri = new URIBuilder().setPath(config.proxy_controller_url() + "/entities/").setParameter("query",query).build();
        final String r = Request.Get(uri).useExpectContinue().execute().returnContent().asString();
        writer.write(r);
    }

    @ResponseBody
    @RequestMapping(value = "/rest/api/v1/entities/", method = RequestMethod.GET, produces = "application/json")
    public void getEntitiesControllerEP(final Writer writer, final HttpServletResponse response, @RequestParam(value = "query", defaultValue = "{}") final String query) throws IOException, URISyntaxException {
        if(!config.proxy_controller()) {
            writer.write("");
            return;
        }

        response.setContentType("application/json");
        URI uri = new URIBuilder().setPath(config.proxy_controller_url() + "/entities/").setParameter("query",query).build();
        final String r = Request.Get(uri).useExpectContinue().execute().returnContent().asString();
        writer.write(r);
    }

    @ResponseBody
    @RequestMapping(value = "/api/v1/checks/", method = RequestMethod.GET, produces = "application/json")
    public void getChecks(final Writer writer, final HttpServletResponse response, @RequestParam(value = "query", defaultValue = "{}") final String query) throws IOException, URISyntaxException {
        if(!config.proxy_controller()) {
            writer.write("");
            return;
        }

        response.setContentType("application/json");
        URI uri = new URIBuilder().setPath(config.proxy_controller_url() + "/checks/all-active-check-definitions").setParameter("query",query).build();
        final String r = Request.Get(uri).useExpectContinue().execute().returnContent().asString();
        writer.write(r);
    }

    @ResponseBody
    @RequestMapping(value = "/rest/api/v1/checks/all-active-check-definitions", method = RequestMethod.GET, produces = "application/json")
    public void getChecksControllerEP(final Writer writer, final HttpServletResponse response, @RequestParam(value = "query", defaultValue = "{}") final String query) throws IOException, URISyntaxException {
        if(!config.proxy_controller()) {
            writer.write("");
            return;
        }

        response.setContentType("application/json");
        URI uri = new URIBuilder().setPath(config.proxy_controller_url() + "/checks/all-active-check-definitions").setParameter("query",query).build();
        final String r = Request.Get(uri).useExpectContinue().execute().returnContent().asString();
        writer.write(r);
    }

    @ResponseBody
    @RequestMapping(value = "/api/v1/alerts/", method = RequestMethod.GET, produces = "application/json")
    public void getAlerts(final Writer writer, final HttpServletResponse response, @RequestParam(value = "query", defaultValue = "{}") final String query) throws IOException, URISyntaxException {
        if(!config.proxy_controller()) {
            writer.write("");
            return;
        }

        response.setContentType("application/json");
        URI uri = new URIBuilder().setPath(config.proxy_controller_url() + "/checks/all-active-alert-definitions").setParameter("query",query).build();
        final String r = Request.Get(uri).useExpectContinue().execute().returnContent().asString();
        writer.write(r);
    }

    @ResponseBody
    @RequestMapping(value = "/rest/api/v1/checks/all-active-alert-definitions", method = RequestMethod.GET, produces = "application/json")
    public void getAlertsControllerEP(final Writer writer, final HttpServletResponse response, @RequestParam(value = "query", defaultValue = "{}") final String query) throws IOException, URISyntaxException {
        if(!config.proxy_controller()) {
            writer.write("");
            return;
        }

        response.setContentType("application/json");
        URI uri = new URIBuilder().setPath(config.proxy_controller_url() + "/checks/all-active-alert-definitions").setParameter("query",query).build();
        final String r = Request.Get(uri).useExpectContinue().execute().returnContent().asString();
        writer.write(r);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
