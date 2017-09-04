package de.zalando.zmon.dataservice;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import de.zalando.zmon.dataservice.config.JaegerConfig;
import de.zalando.zmon.dataservice.config.LightStepConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.opentracing.NoopTracerFactory;
import io.opentracing.Tracer;
import com.instana.opentracing.InstanaTracerFactory;
import java.net.MalformedURLException;

/**
 * Created by jmussler on 4/21/15.
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
public class Application {

    @Autowired
    private DataServiceConfigProperties config;

    private final Logger logger = LoggerFactory.getLogger(Application.class);

    @Bean
    public Tracer tracer() {
        Tracer tracer = NoopTracerFactory.create();
        if ("jaeger".equals(config.getTracingProvider().toLowerCase())){
            tracer = new JaegerConfig(config).getTracer();
        } else if ("lightstep".equals(config.getTracingProvider().toLowerCase())){
            try {
                tracer = new LightStepConfig(config).getTracer();
            } catch (MalformedURLException e) {
                 logger.error("Lightstep host/port configuration incorrect. LightStep host used:" + config.getLightStepHost() + " LightStep port used:" + config.getLightStepPort(), e.getMessage());
            }
        } else if ("instana".equals(config.getTracingProvider().toLowerCase())) {
            tracer = InstanaTracerFactory.create();
        }
        return tracer;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
