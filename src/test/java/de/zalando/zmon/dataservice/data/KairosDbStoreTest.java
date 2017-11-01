package de.zalando.zmon.dataservice.data;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
import java.util.Arrays;
import java.util.List;

@ContextConfiguration
public class KairosDbStoreTest extends AbstractControllerTest {

    @Rule
    public final WireMockRule wireMockRule = new WireMockRule(10081);

    @Autowired
    private RedisDataPointsStore redisDataPointsStore;

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
        KairosDBStore kairosDb = new KairosDBStore(config, metrics, redisDataPointsStore);
        kairosDb.store(Fixture.buildWorkerResult());
        verify(metrics, never()).markKairosError();
    }

    @Test
    public void testInvalidWorkerResult() {
        KairosDBStore kairosDb = new KairosDBStore(config, metrics, redisDataPointsStore);
        for(WorkerResult wr: new WorkerResult[]{null, new WorkerResult()}) {
            kairosDb.store(wr);
            verify(metrics, never()).incKairosDBDataPoints(anyLong());
        }
    }

    @Configuration
    static class TestConfig {

        @Bean
        public DataServiceConfigProperties dataServiceConfigProperties() {
            DataServiceConfigProperties props = new DataServiceConfigProperties();
            List<String> kairosdbHosts = new ArrayList<>(1);
            kairosdbHosts.add("http://localhost:10081");
            props.setKairosdbWriteUrls(Arrays.asList(kairosdbHosts));
            props.setLogKairosdbRequests(true);
            props.setLogKairosdbErrors(true);
            return props;
        }

        @Bean
        public DataServiceMetrics dataServiceMetrics() {
            return Mockito.mock(DataServiceMetrics.class);
        }

        @Bean
        public RedisDataPointsStore redisDataPointsStore() {return Mockito.mock(RedisDataPointsStore.class);}
    }

}
