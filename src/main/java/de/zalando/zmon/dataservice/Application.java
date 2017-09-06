package de.zalando.zmon.dataservice;

import de.zalando.zmon.dataservice.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private OpenTracingConfigProperties openTracingConfig;

    @Bean
    public Tracer tracer() {
        Tracer tracer = NoopTracerFactory.create();
        if (OpenTracingProvider.JAEGER.equalsIgnoreCase(openTracingConfig.getTracingProvider())){
            tracer = new JaegerConfig(openTracingConfig).generateTracer();
        } else if (OpenTracingProvider.LIGHTSTEP.equalsIgnoreCase(openTracingConfig.getTracingProvider())){
            //TODO: Enable lightstep tracing only when lightstep infra is available after proper testing
            //tracer = new LightStepConfig(openTracingConfig).generateTracer();
        } else if (OpenTracingProvider.INSTANA.equalsIgnoreCase(openTracingConfig.getTracingProvider())) {
            tracer = InstanaTracerFactory.create();
        }
        return tracer;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
