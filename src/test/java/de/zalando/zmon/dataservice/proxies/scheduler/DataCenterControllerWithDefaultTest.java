package de.zalando.zmon.dataservice.proxies.scheduler;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

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
    @Import({ SchedulerConfig.class, ObjectMapperConfig.class })
    static class TestConfig {

        @Bean
        public DataServiceConfigProperties dataServiceConfigProperties() {
            DataServiceConfigProperties props = new DataServiceConfigProperties();
            props.setProxyScheduler(true);
            props.setProxySchedulerUrl("http://localhost:9999");
            return props;
        }

        @Bean
        public DataServiceMetrics dataServiceMetrics() {
            return Mockito.mock(DataServiceMetrics.class);
        }
    }
}
