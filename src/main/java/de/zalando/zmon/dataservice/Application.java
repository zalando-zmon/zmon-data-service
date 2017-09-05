package de.zalando.zmon.dataservice;

import de.zalando.zmon.dataservice.config.*;
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
    private OpenTracingConfigProperties openTracingConfig;

    private final Logger logger = LoggerFactory.getLogger(Application.class);

    @Bean
    public Tracer tracer() {
        Tracer tracer = NoopTracerFactory.create();
        if (JAEGER.equalsIgnoreCase(openTracingConfig.getTracingProvider())){
            tracer = new JaegerConfig(openTracingConfig).generateTracer();
        } else if (LIGHTSTEP.equalsIgnoreCase(openTracingConfig.getTracingProvider())){
                tracer = new LightStepConfig(openTracingConfig).generateTracer();
        } else if (INSTANA.equalsIgnoreCase(openTracingConfig.getTracingProvider())) {
            tracer = InstanaTracerFactory.create();
        }
        return tracer;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
