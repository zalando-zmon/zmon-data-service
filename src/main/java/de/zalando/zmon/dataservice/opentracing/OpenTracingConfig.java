package de.zalando.zmon.dataservice.opentracing;

import com.instana.opentracing.InstanaTracerFactory;
import de.zalando.zmon.dataservice.config.OpenTracingConfigProperties;
import io.opentracing.NoopTracerFactory;
import io.opentracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTracingConfig {

    @Autowired
    private OpenTracingConfigProperties openTracingConfig;

    @Bean
    public Tracer tracer() {
        Tracer tracer = NoopTracerFactory.create();
        if (OpenTracingProvider.JAEGER.equalsIgnoreCase(openTracingConfig.getTracingProvider())){
            tracer = new JaegerConfig(openTracingConfig).generateTracer();
        } else if (OpenTracingProvider.LIGHTSTEP.equalsIgnoreCase(openTracingConfig.getTracingProvider())){
            tracer = new LightStepConfig(openTracingConfig).generateTracer();
        } else if (OpenTracingProvider.INSTANA.equalsIgnoreCase(openTracingConfig.getTracingProvider())) {
            tracer = InstanaTracerFactory.create();
        }
        return tracer;
    }
}
