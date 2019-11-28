package de.zalando.zmon.dataservice.proxies;

import org.springframework.cloud.circuitbreaker.springretry.SpringRetryCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.springretry.SpringRetryConfigBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialRandomBackOffPolicy;

@Configuration
public class ProxyConfig {
    @Bean
    public SpringRetryCircuitBreakerFactory defaultCustomizer() {
        final SpringRetryCircuitBreakerFactory factory = new SpringRetryCircuitBreakerFactory();
        factory.configureDefault(
                id -> new SpringRetryConfigBuilder(id)
                        .backOffPolicy(new ExponentialRandomBackOffPolicy())
                        .build());
        return factory;
    }


}
