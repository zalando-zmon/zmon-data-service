package de.zalando.zmon.dataservice.data;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableList;
import de.zalando.zmon.dataservice.AbstractControllerTest;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

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


    @Before
    public void setUp() {
        wireMockRule.stubFor(post(urlPathEqualTo("/api/v1/datapoints"))
                .willReturn(aResponse().withStatus(200).withBody("{}").withFixedDelay(200)));
    }

    @Test
    public void writeWorkerResult() {
        KairosDBStore kairosDb = new KairosDBStore(config, metrics, dataPointsQueryStore);
        kairosDb.store(Fixture.buildWorkerResult());
        verify(dataPointsQueryStore, atMost(1)).store(anyString());
        verify(metrics, never()).markKairosError();
        verify(metrics, never()).markKairosHostErrors(anyLong());
    }

    @Test
    public void testInvalidWorkerResult() {
        KairosDBStore kairosDb = new KairosDBStore(config, metrics, dataPointsQueryStore);
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
    }

}
