package de.zalando.zmon.dataservice.proxies.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.components.CustomObjectMapper;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import de.zalando.zmon.dataservice.proxies.ProxyConfig;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ProxyConfig.class})
public class EntitiesConfig {

	private final DataServiceConfigProperties config;

	private final ObjectMapper customObjectMapper;

	private final DataServiceMetrics metrics;

	private final CircuitBreakerFactory cbFactory;

	public EntitiesConfig(DataServiceConfigProperties config,
						  @CustomObjectMapper ObjectMapper customObjectMapper,
						  DataServiceMetrics metrics,
						  CircuitBreakerFactory cbFactory) {
		this.config = config;
		this.customObjectMapper = customObjectMapper;
		this.metrics = metrics;
		this.cbFactory = cbFactory;
	}

	@Bean
	public EntitiesService entitiesService() {
		if (config.isProxyController()) {
			return new DefaultEntitiesService(customObjectMapper, config, metrics, cbFactory);
		} else {
			return new NoOpEntitiesService();
		}
	}

}
