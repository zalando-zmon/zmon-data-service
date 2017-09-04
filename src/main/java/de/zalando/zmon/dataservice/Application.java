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

/**
 * Created by jmussler on 4/21/15.
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
public class Application {

    private static final String JAEGER = "jaeger";
    private static final String LIGHTSTEP = "lightstep";
    private static final String INSTANA = "instana";

    @Autowired
    private DataServiceConfigProperties config;

    private final Logger logger = LoggerFactory.getLogger(Application.class);

    @Bean
    public Tracer tracer() {
        Tracer tracer = NoopTracerFactory.create();
        if (JAEGER.equalsIgnoreCase(config.getTracingProvider().toLowerCase())){
            tracer = new JaegerConfig(config).generateTracer();
        } else if (LIGHTSTEP.equalsIgnoreCase(config.getTracingProvider().toLowerCase())){
                tracer = new LightStepConfig(config).generateTracer();
        } else if (INSTANA.equalsIgnoreCase(config.getTracingProvider().toLowerCase())) {
            tracer = InstanaTracerFactory.create();
        }
        return tracer;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
