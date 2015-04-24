package de.zalando.zmon.dataservice;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Bean
    JedisPool getPool(@Value("${redis.host:localhost}") String host, @Value("${redis.port:6379}") int port) {
        return new JedisPool(host, port);
    }

    @RequestMapping(value="/api/v1/data/{accountid}/{checkid}/", method= RequestMethod.PUT, consumes = {"text/plain", "application/json"})
    void putData(@PathVariable(value="checkid") int checkId, @PathVariable(value="accountid") String accountId, @RequestBody String data) {
        metrics.markRate();
        metrics.markAccount(accountId, data.length());
        metrics.markCheck(checkId, data.length());

        try {
            WorkerResult wr = mapper.readValue(data, WorkerResult.class);
            storage.store(wr);

            // TODO KairosDB write
        }
        catch(Exception e) {
            LOG.error("",e);
        }
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
