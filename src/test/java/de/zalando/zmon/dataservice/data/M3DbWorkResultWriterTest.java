package de.zalando.zmon.dataservice.data;

import com.codahale.metrics.Timer;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

/** @author raparida on 15.02.19 */
public class M3DbWorkResultWriterTest {
  private M3DbStore m3DbStore;
  private DataServiceMetrics metrics;
  private WorkerResult wrMock;
  private DataServiceConfigProperties configMock;

  @Before
  public void setUp() {
    metrics = Mockito.mock(DataServiceMetrics.class);
    Timer timer = Mockito.mock(Timer.class);
    Timer.Context context = Mockito.mock(Timer.Context.class);
    Mockito.when(timer.time()).thenReturn(context);
    Mockito.when(metrics.getM3DBTimer()).thenReturn(timer);

    wrMock = Mockito.mock(WorkerResult.class);
    m3DbStore = Mockito.mock(M3DbStore.class);
    configMock = Mockito.mock(DataServiceConfigProperties.class);
  }

  @After
  public void tearDown() {
    Mockito.reset(wrMock, m3DbStore, metrics, configMock);
  }

  @Test
  public void onEmptyOptional() {
    M3DbWorkResultWriter writer = new M3DbWorkResultWriter(configMock, m3DbStore, metrics);
    writer.write(Fixture.writeData(Optional.empty()));
    Mockito.verify(m3DbStore, Mockito.never()).store(Fixture.buildGenericMetrics());
    Mockito.verify(metrics, Mockito.never()).markM3DbError();
  }

  @Test
  public void onEmptyResults() {
    M3DbWorkResultWriter writer = new M3DbWorkResultWriter(configMock, m3DbStore, metrics);
    WorkerResult wr = Mockito.mock(WorkerResult.class);
    writer.write(Fixture.writeData(Optional.ofNullable(wr)));
    Mockito.verify(m3DbStore, Mockito.never()).store(Fixture.buildGenericMetrics());
    Mockito.verify(metrics, Mockito.never()).markM3DbError();
  }

  @Test
  public void nonEmptyResult() {
    M3DbWorkResultWriter writer = new M3DbWorkResultWriter(configMock, m3DbStore, metrics);
    WorkerResult wr = Fixture.buildWorkerResult();
    writer.write(Fixture.writeData(Optional.ofNullable(wr)));

    Mockito.verify(m3DbStore, Mockito.times(1)).store(Mockito.anyList());
    Mockito.verify(metrics, Mockito.never()).markM3DbError();
  }

  @Test
  public void nonEmptyOptionalWithStoreException() {
    Mockito.doThrow(new RuntimeException("test")).when(m3DbStore).store(Mockito.anyList());

    M3DbWorkResultWriter writer = new M3DbWorkResultWriter(configMock, m3DbStore, metrics);

    WorkerResult wr = Fixture.buildWorkerResult();
    writer.write(Fixture.writeData(Optional.ofNullable(wr)));

    Mockito.verify(m3DbStore, Mockito.times(1)).store(Mockito.anyList());
    Mockito.verify(metrics, Mockito.times(1)).markM3DbError();
  }
}
