package de.zalando.zmon.dataservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;

import de.zalando.zmon.dataservice.DataServiceMetrics;

@Configuration
public class MetricsConfig {

    @Bean
    public DataServiceMetrics dataServiceMetrics(MetricRegistry metricsRegitry) {
        return new DataServiceMetrics(metricsRegitry);
    }

}
