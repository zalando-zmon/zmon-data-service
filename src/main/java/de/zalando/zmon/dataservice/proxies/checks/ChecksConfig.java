package de.zalando.zmon.dataservice.proxies.checks;

import de.zalando.zmon.dataservice.TokenWrapper;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChecksConfig {

	private final DataServiceConfigProperties config;

	private final TokenWrapper wrapper;

	private final CircuitBreakerFactory cbFactory;

	public ChecksConfig(DataServiceConfigProperties config, TokenWrapper wrapper, CircuitBreakerFactory cbFactory) {
		this.config = config;
		this.wrapper = wrapper;
		this.cbFactory = cbFactory;
	}

	@Bean
	public ChecksService checksService() {
		if (config.isProxyController()) {
			ChecksService controller = new DefaultChecksService(config, cbFactory);
			if (!config.isProxyControllerCache()) {
				return controller;
			}

			return new CachingCheckService(config, controller, wrapper);
		} else {
			return new NoOpChecksService();
		}
	}
}
