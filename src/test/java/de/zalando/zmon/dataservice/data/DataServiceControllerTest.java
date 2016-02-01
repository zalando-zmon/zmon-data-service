package de.zalando.zmon.dataservice.data;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;

import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.zalando.zmon.dataservice.AbstractControllerTest;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.components.CustomObjectMapper;
import de.zalando.zmon.dataservice.components.DefaultObjectMapper;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import de.zalando.zmon.dataservice.config.ObjectMapperConfig;

@ContextConfiguration
public class DataServiceControllerTest extends AbstractControllerTest {

    @Autowired
    private DataServiceMetrics metrics;

    private RedisDataStore storage;

    private KairosDBStore kairosStore;

    @Autowired
    @DefaultObjectMapper
    private ObjectMapper defaultObjectMapper;

    @Autowired
    @CustomObjectMapper
    private ObjectMapper customObjectMapper;

    DataServiceController controller;

    List<WorkResultWriter> workResultWriter;

    @Before
    public void setUp() {

        Timer timer = Mockito.mock(Timer.class);
        Context context = Mockito.mock(Context.class);
        Mockito.when(timer.time()).thenReturn(context);
        Mockito.when(metrics.getKairosDBTimer()).thenReturn(timer);

        storage = Mockito.mock(RedisDataStore.class);
        kairosStore = Mockito.mock(KairosDBStore.class);

        controller = new DataServiceController(storage, metrics, defaultObjectMapper, customObjectMapper,
                workResultWriter);
    }

    @After
    public void cleanMocks() {
        Mockito.reset(storage, kairosStore, metrics);
    }

    @Test
    public void extract() {
        Optional<WorkerResult> wrOptional = controller.extractAndFilter("{}", "stups", 13);
        Assertions.assertThat(wrOptional.get()).isNotNull();
        Assertions.assertThat(wrOptional.get().results).isEmpty();
    }

    @Test
    public void extractWithException() {
        // we use null to fail
        Optional<WorkerResult> wrOptional = controller.extractAndFilter(null, "stups", 13);
        Assertions.assertThat(wrOptional.isPresent()).isFalse();
        Mockito.verify(metrics, Mockito.atLeast(1)).markParseError();
    }

    @Test
    public void tialRunWithException() {
        DataServiceController controllerSpy = Mockito.spy(controller);

        controllerSpy.putTrialRunData("");

        Mockito.verify(metrics, Mockito.atLeastOnce()).markTrialRunData();
        Mockito.verify(metrics, Mockito.atLeastOnce()).markTrialRunError();
    }

    @Test
    public void tialRun() throws IOException {
        DataServiceController controllerSpy = Mockito.spy(controller);

        controllerSpy.putTrialRunData(resourceToString(jsonResource("trialRun")));

        Mockito.verify(metrics, Mockito.atLeastOnce()).markTrialRunData();
        Mockito.verify(metrics, Mockito.never()).markTrialRunError();
    }

    @Configuration
    @Import({ ObjectMapperConfig.class })
    static class TestConfig {

        @Bean
        public DataServiceConfigProperties dataServiceConfigProperties(Environment env) {
            return new DataServiceConfigProperties(env);
        }

        @Bean
        public DataServiceMetrics dataServiceMetrics() {
            return Mockito.mock(DataServiceMetrics.class);
        }
    }
}
