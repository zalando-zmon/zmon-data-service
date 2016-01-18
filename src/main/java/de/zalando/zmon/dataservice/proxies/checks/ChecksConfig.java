package de.zalando.zmon.dataservice.proxies.checks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

@Configuration
public class ChecksConfig {

	@Autowired
	private DataServiceConfigProperties config;

	@Bean
	public ChecksService checksService() {
		if (!config.isProxyController()) {
			return new NoOpChecksService();
		} else {
			return new DefaultChecksService(config);
		}
	}
}
