package de.zalando.zmon.dataservice.proxies.checks;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import de.zalando.zmon.dataservice.TokenWrapper;
import org.junit.Before;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import de.zalando.zmon.dataservice.config.ObjectMapperConfig;

/**
 * 
 * @author jbellmann
 *
 */
@ContextConfiguration
public class ChecksControllerWithDefaultCheckServiceTest extends AbstractCheckControllerTest {

    @Before
    public void configureWireMockForCheck() {
        wireMockRule.stubFor(get(urlPathEqualTo("/checks/all-active-check-definitions"))
                .willReturn(aResponse().withStatus(200).withBody("").withFixedDelay(200)));
        wireMockRule.stubFor(get(urlPathEqualTo("/checks/all-active-alert-definitions"))
                .willReturn(aResponse().withStatus(200).withBody("").withFixedDelay(200)));
    }

    @Configuration
    @Import({ ChecksConfig.class, ObjectMapperConfig.class })
    static class TestConfig {

        @Bean
        public DataServiceConfigProperties dataServiceConfigProperties() {
            DataServiceConfigProperties props = new DataServiceConfigProperties();
            props.setProxyController(true);
            props.setProxyControllerUrl("http://localhost:9999");
            return props;
        }

        @Bean
        public TokenWrapper getTokenWrapper() {
            return new TokenWrapper("ABC");
        }

        @Bean
        public DataServiceMetrics dataServiceMetrics() {
            return Mockito.mock(DataServiceMetrics.class);
        }
    }
}
