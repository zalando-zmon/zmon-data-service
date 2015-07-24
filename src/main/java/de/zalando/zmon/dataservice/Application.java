package de.zalando.zmon.dataservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
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

    @Bean
    @Autowired
    JedisPool getPool(DataServiceConfig config) {
        return new JedisPool(config.redis_host(), config.redis_port());
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

    @RequestMapping(value="/api/v1/data/{account}/{checkid}/", method= RequestMethod.PUT, consumes = {"text/plain", "application/json"})
    void putData(@PathVariable(value="checkid") int checkId, @PathVariable(value="account") String accountId, @RequestBody String data) {

        LOG.info(data);

        metrics.markRate();
        metrics.markAccount(accountId, data.length());
        metrics.markCheck(checkId, data.length());

        try {
            WorkerResult wr = valueMapper.readValue(data, new TypeReference<WorkerResult>(){});

            // make sure that the unique account it is actually in th aws:<accountid> string
            // this should protect us from wrongly configured schedulers that execute the wrong checks
            wr.results = wr.results.stream().filter(x->x.entity_id.contains(accountId)).collect(Collectors.toList());
            storage.store(wr);
            kairosStore.store(wr);
        }
        catch(Exception e) {
            metrics.markError();
        }
    }

    @ResponseBody
    @RequestMapping(value = "/api/v1/entities/", method = RequestMethod.PUT, produces = "application/json")
    public void getEntities(final Writer writer, final HttpServletResponse response, @RequestBody(required=true) final JsonNode node) throws IOException, URISyntaxException {
        if(!config.proxy_controller()) {
            writer.write("");
            return;
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
