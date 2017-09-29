package de.zalando.zmon.dataservice.proxies.checks;

import de.zalando.zmon.dataservice.TokenWrapper;
import de.zalando.zmon.dataservice.data.TestingProperties;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import de.zalando.zmon.dataservice.config.ObjectMapperConfig;

import static org.mockito.Mockito.mock;

@ContextConfiguration
public class ChecksControllerWithNoOpServiceTest extends AbstractCheckControllerTest {

    @Configuration
    @Import({ ChecksConfig.class, ObjectMapperConfig.class, TestingProperties.class })
    static class TestConfig {

        @Bean
        public DataServiceMetrics dataServiceMetrics() {
            return mock(DataServiceMetrics.class);
        }

        @Bean
        public TokenWrapper getTokenWrapper() {
            return new TokenWrapper("ABC");
        }
    }
}
