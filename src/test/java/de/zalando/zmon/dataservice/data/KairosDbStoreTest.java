package de.zalando.zmon.dataservice.data;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import de.zalando.zmon.dataservice.AbstractControllerTest;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

import java.util.List;

@ContextConfiguration
public class KairosDbStoreTest extends AbstractControllerTest {

    @Rule
    public final WireMockRule wireMockRule = new WireMockRule(10081);

    @Autowired
    private DataPointsQueryStore dataPointsQueryStore;

    @Autowired
    private DataServiceConfigProperties config;

    @Autowired
    private DataServiceMetrics metrics;

    @Autowired
    private WhitelistedChecks whitelistedChecks;

    @Before
    public void setUp() {
        wireMockRule.stubFor(post(urlPathEqualTo("/api/v1/datapoints"))
                .willReturn(aResponse().withStatus(200).withBody("{}").withFixedDelay(200)));
    }

    @Test
    public void writeWorkerResult() {
        KairosDBStore kairosDb = new KairosDBStore(config, metrics, dataPointsQueryStore, whitelistedChecks);
        kairosDb.store(Fixture.buildWorkerResult());
        verify(dataPointsQueryStore, atMost(1)).store(anyString());
        verify(metrics, never()).markKairosError();
        verify(metrics, never()).markKairosHostErrors(anyLong());
    }

    @Test
    public void testInvalidWorkerResult() {
        KairosDBStore kairosDb = new KairosDBStore(config, metrics, dataPointsQueryStore, whitelistedChecks);
        for (WorkerResult wr : new WorkerResult[]{null, new WorkerResult()}) {
            kairosDb.store(wr);
            verify(metrics, never()).incKairosDBDataPoints(anyLong());
            verify(dataPointsQueryStore, never()).store(anyString());
        }
    }

    @Configuration
    static class TestConfig {

        @Bean
        public DataServiceConfigProperties dataServiceConfigProperties() {
            DataServiceConfigProperties props = new DataServiceConfigProperties();
            props.setKairosdbWriteUrls(ImmutableList.of(ImmutableList.of("http://localhost:10081")));
            props.setLogKairosdbRequests(true);
            props.setLogKairosdbErrors(true);
            return props;
        }

        @Bean
        public DataServiceMetrics dataServiceMetrics() {
            return mock(DataServiceMetrics.class);
        }

        @Bean
        public DataPointsQueryStore dataPointsStore() {
            return mock(DataPointsQueryStore.class);
        }

        @Bean
        public WhitelistedChecks whitelistedChecks() {
            WhitelistedChecks mock = mock(WhitelistedChecks.class);
            List<Integer> mockList = mock(List.class);
            when(mockList.contains(any())).thenReturn(true);
            when(mock.getWhitelist()).thenReturn(mockList);
            return mock;
        }

    }

}
