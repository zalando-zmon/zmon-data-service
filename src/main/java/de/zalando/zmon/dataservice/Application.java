package de.zalando.zmon.dataservice;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

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

    @RequestMapping(value="/api/v1/data/{checkid}/{accountid}/", method= RequestMethod.PUT, consumes = {"text/plain", "application/json"})
    void putData(@PathVariable(value="checkid") int checkId, @PathVariable(value="accountid") String accountId, @RequestBody String data) {
        LOG.info("data: {}", data);

        metrics.markRate();
        metrics.markAccount(accountId, data.length());
        metrics.markCheck(checkId, data.length());
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
