package de.zalando.zmon.dataservice.proxies.checks;

import de.zalando.zmon.dataservice.TokenWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

@Configuration
public class ChecksConfig {

	@Autowired
	private DataServiceConfigProperties config;

	@Autowired
	private TokenWrapper wrapper;

	@Bean
	public ChecksService checksService() {
		if (config.isProxyController()) {

			ChecksService controller = new DefaultChecksService(config);

			if(!config.isProxyControllerCache()) {
				return controller;
			}

			return new CachingCheckService(config, controller, wrapper);
		} else {
			return new NoOpChecksService();
		}
	}
}
