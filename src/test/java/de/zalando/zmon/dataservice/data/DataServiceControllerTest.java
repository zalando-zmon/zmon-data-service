package de.zalando.zmon.dataservice.data;

import java.io.IOException;
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
	private DataServiceConfigProperties config;

	@Autowired
	private DataServiceMetrics metrics;

	private AppMetricsClient appMetricsClient;

	private RedisDataStore storage;

	private KairosDBStore kairosStore;

	@Autowired
	@DefaultObjectMapper
	private ObjectMapper defaultObjectMapper;

	@Autowired
	@CustomObjectMapper
	private ObjectMapper customObjectMapper;

	DataServiceController controller;

	@Before
	public void setUp() {

		Timer timer = Mockito.mock(Timer.class);
		Context context = Mockito.mock(Context.class);
		Mockito.when(timer.time()).thenReturn(context);
		Mockito.when(metrics.getKairosDBTimer()).thenReturn(timer);

		storage = Mockito.mock(RedisDataStore.class);
		kairosStore = Mockito.mock(KairosDBStore.class);
		appMetricsClient = Mockito.mock(AppMetricsClient.class);

		controller = new DataServiceController(config, appMetricsClient, storage, kairosStore, metrics,
				defaultObjectMapper, customObjectMapper);
	}
	
	@After
	public void cleanMocks(){
		Mockito.reset(storage,kairosStore, metrics);
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
	public void storeResult() {
		controller.writeRedis(new WorkerResult(), "test", 13);
		Mockito.verify(storage, Mockito.atLeast(1)).store(Mockito.any(WorkerResult.class));
		Mockito.verify(metrics, Mockito.never()).markRedisError();
	}
	
	@Test
	public void storeResultWithError() {
		Mockito.doThrow(new RuntimeException()).when(storage).store(Mockito.any(WorkerResult.class));
		controller.writeRedis(new WorkerResult(), "test", 13);
		Mockito.verify(metrics, Mockito.atLeastOnce()).markRedisError();
	}

	@Test
	public void storeKairosResult() {

		controller.writeKairos(new WorkerResult(), "test", 13);
		Mockito.verify(kairosStore, Mockito.atLeast(1)).store(Mockito.any(WorkerResult.class));
		Mockito.verify(metrics, Mockito.never()).markRedisError();
	}
	
	@Test
	public void storeKairosResultWithError() {
		Mockito.doThrow(new RuntimeException()).when(kairosStore).store(Mockito.any(WorkerResult.class));
		controller.writeKairos(new WorkerResult(), "test", 13);
		Mockito.verify(metrics, Mockito.atLeastOnce()).markKairosError();
	}
	
	@Test
	public void doNotInvokeWriterMethodsWhenWokerResultOptionalIsEmpty() {
		DataServiceController controllerSpy = Mockito.spy(controller);
		
		controllerSpy.putData(13, "stups", "");

		Mockito.verify(controllerSpy, Mockito.never()).writeRedis(Mockito.any(WorkerResult.class), Mockito.eq(""), Mockito.anyInt());
		Mockito.verify(controllerSpy, Mockito.never()).writeKairos(Mockito.any(WorkerResult.class), Mockito.eq(""), Mockito.anyInt());
		Mockito.verify(controllerSpy, Mockito.never()).writeApplicationMetrics(Mockito.any(WorkerResult.class), Mockito.eq(""));
		
	}
	
	@Test
	public void doInvokeWriterMethodsWhenWokerResultOptionalIsNotEmpty() {

		DataServiceController controllerSpy = Mockito.spy(controller);

		controllerSpy.putData(13, "stups", "{}");

		Mockito.verify(controllerSpy, Mockito.times(1)).writeRedis(Mockito.any(WorkerResult.class), Mockito.eq("{}"), Mockito.anyInt());
		Mockito.verify(controllerSpy, Mockito.times(1)).writeKairos(Mockito.any(WorkerResult.class), Mockito.eq("{}"), Mockito.anyInt());
		Mockito.verify(controllerSpy, Mockito.times(1)).writeApplicationMetrics(Mockito.any(WorkerResult.class), Mockito.eq("{}"));
	}
	
	@Test
	public void tialRunWithException(){
		DataServiceController controllerSpy = Mockito.spy(controller);

		controllerSpy.putTrialRunData("");

		Mockito.verify(metrics, Mockito.atLeastOnce()).markTrialRunData();
		Mockito.verify(metrics, Mockito.atLeastOnce()).markTrialRunError();
	}
	
	@Test
	public void tialRun() throws IOException{
		DataServiceController controllerSpy = Mockito.spy(controller);

		controllerSpy.putTrialRunData(resourceToString(jsonResource("trialRun")));

		Mockito.verify(metrics, Mockito.atLeastOnce()).markTrialRunData();
		Mockito.verify(metrics, Mockito.never()).markTrialRunError();
	}
	
	@Configuration
	@Import({ObjectMapperConfig.class })
	static class TestConfig {

		@Bean
		public DataServiceConfigProperties dataServiceConfigProperties() {
			return new DataServiceConfigProperties();
		}

		@Bean
		public DataServiceMetrics dataServiceMetrics(){
			return Mockito.mock(DataServiceMetrics.class);
		}
	}
}
