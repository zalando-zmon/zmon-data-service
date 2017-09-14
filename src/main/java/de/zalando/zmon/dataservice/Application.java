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
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
