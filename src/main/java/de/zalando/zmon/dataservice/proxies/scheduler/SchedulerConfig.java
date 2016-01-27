package de.zalando.zmon.dataservice.proxies.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

@Configuration
public class SchedulerConfig {

	@Autowired
	private DataServiceConfigProperties config;

	@Bean
	public SchedulerService schedulerProxy() {
		if (config.isProxyScheduler()) {
			return new DefaultSchedulerService(config);
		} else {
			return new NoOpSchedulerService();
		}
	}
}
