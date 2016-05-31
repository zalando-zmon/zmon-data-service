package de.zalando.zmon.dataservice.data;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import de.zalando.zmon.dataservice.AbstractControllerTest;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

import java.util.ArrayList;
import java.util.List;

@ContextConfiguration
public class KairosDbStoreTest extends AbstractControllerTest {

    @Rule
    public final WireMockRule wireMockRule = new WireMockRule(10081);

    @Autowired
    private DataServiceConfigProperties config;

    @Autowired
    private DataServiceMetrics metrics;

    @Before
    public void setUp() {
        wireMockRule.stubFor(post(urlPathEqualTo("/api/v1/datapoints"))
                .willReturn(aResponse().withStatus(200).withBody("{}").withFixedDelay(200)));
    }

    @Test
    public void writeWorkerResult() {
        KairosDBStore kairosDb = new KairosDBStore(config, metrics);
        kairosDb.store(Fixture.buildWorkerResult());
        Mockito.verify(metrics, Mockito.never()).markKairosError();
    }

    @Configuration
    static class TestConfig {

        @Bean
        public DataServiceConfigProperties dataServiceConfigProperties() {
            DataServiceConfigProperties props = new DataServiceConfigProperties();
            List<String> kairosdbHosts = new ArrayList<>(1);
            kairosdbHosts.add("http://localhost:10081");
            props.setKairosdbWriteUrls(kairosdbHosts);
            props.setLogKairosdbRequests(true);
            props.setLogKairosdbErrors(true);
            return props;
        }

        @Bean
        public DataServiceMetrics dataServiceMetrics() {
            return Mockito.mock(DataServiceMetrics.class);
        }
    }

}
