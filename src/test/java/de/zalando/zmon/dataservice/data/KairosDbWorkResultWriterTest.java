package de.zalando.zmon.dataservice.data;

import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

public class KairosDbWorkResultWriterTest {

  private KairosDBStore kairosDbStore;
  private DataServiceMetrics metrics;
  private WorkerResult wrMock;
  private DataServiceConfigProperties configMock;

  @Before
  public void setUp() {
    metrics = Mockito.mock(DataServiceMetrics.class);
    Timer timer = Mockito.mock(Timer.class);
    Context context = Mockito.mock(Context.class);
    Mockito.when(timer.time()).thenReturn(context);
    Mockito.when(metrics.getKairosDBTimer()).thenReturn(timer);

    wrMock = Mockito.mock(WorkerResult.class);
    kairosDbStore = Mockito.mock(KairosDBStore.class);
    configMock = Mockito.mock(DataServiceConfigProperties.class);
  }

  @After
  public void tearDown() {
    Mockito.reset(wrMock, kairosDbStore, metrics, configMock);
  }

  @Test
  public void onEmptyOptional() {
    KairosDbWorkResultWriter writer =
        new KairosDbWorkResultWriter(configMock, kairosDbStore, metrics);
    writer.write(Fixture.writeData(Optional.empty()));
    Mockito.verify(kairosDbStore, Mockito.never()).store(Fixture.buildGenericMetrics());
    Mockito.verify(metrics, Mockito.never()).markKairosError();
  }

  @Test
  public void nonEmptyOptional() {
    KairosDbWorkResultWriter writer =
        new KairosDbWorkResultWriter(configMock, kairosDbStore, metrics);
    WorkerResult wr = Mockito.mock(WorkerResult.class);
    writer.write(Fixture.writeData(Optional.ofNullable(wr)));
    Mockito.verify(kairosDbStore, Mockito.never()).store(Fixture.buildGenericMetrics());
    Mockito.verify(metrics, Mockito.never()).markKairosError();
  }

  @Test
  public void nonEmptyOptionalWithStoreException() {
    Mockito.doThrow(new RuntimeException("test"))
        .when(kairosDbStore)
        .store(Fixture.buildGenericMetrics());

    KairosDbWorkResultWriter writer =
        new KairosDbWorkResultWriter(configMock, kairosDbStore, metrics);

    WorkerResult wr = Fixture.buildWorkerResult();
    writer.write(Fixture.writeData(Optional.ofNullable(wr)));

    //Mockito.verify(kairosDbStore, Mockito.times(1)).store(Fixture.buildGenericMetrics());
    Mockito.verify(metrics, Mockito.times(1)).markKairosError();
  }
}
