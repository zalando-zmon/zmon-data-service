package de.zalando.zmon.dataservice.proxies.entities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.components.CustomObjectMapper;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

@Configuration
public class EntitiesConfig {

	@Autowired
	private DataServiceConfigProperties config;

	@Autowired
	@CustomObjectMapper
	private ObjectMapper customObjectMapper;

	@Autowired
	private DataServiceMetrics metrics;

	@Bean
	public EntitiesService entitiesService() {
		if (config.isProxyController()) {
			return new DefaultEntitiesService(customObjectMapper, config, metrics);
		} else {
			return new NoOpEntitiesService();
		}
	}

}
