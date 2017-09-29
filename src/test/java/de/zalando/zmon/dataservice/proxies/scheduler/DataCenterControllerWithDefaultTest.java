package de.zalando.zmon.dataservice.proxies.scheduler;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.mockito.Mockito.mock;

import de.zalando.zmon.dataservice.data.TestingProperties;
import org.junit.Before;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import de.zalando.zmon.dataservice.config.ObjectMapperConfig;

@ContextConfiguration
public class DataCenterControllerWithDefaultTest extends AbstractDataCenterControllerTest {

    @Before
    public void configureWireMockForCheck() {
        wireMockRule.stubFor(get(urlPathEqualTo("/trial-runs/htg"))
                .willReturn(aResponse().withStatus(200).withBody("").withFixedDelay(200)));
        wireMockRule.stubFor(get(urlPathEqualTo("/instant-evaluations/htg"))
                .willReturn(aResponse().withStatus(200).withBody("").withFixedDelay(200)));
    }

    @Configuration
    @Import({ SchedulerConfig.class, ObjectMapperConfig.class, TestingProperties.class })
    static class TestConfig {

        public TestConfig(DataServiceConfigProperties props) {
            props.setProxyScheduler(true);
            props.setProxySchedulerUrl("http://localhost:9999");
        }

        @Bean
        public DataServiceMetrics dataServiceMetrics() {
            return mock(DataServiceMetrics.class);
        }
    }
}
