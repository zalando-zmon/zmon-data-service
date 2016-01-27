package de.zalando.zmon.dataservice.data;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

import de.zalando.zmon.dataservice.DataServiceMetrics;

public class KairosDbWorkResultWriterTest {

    private KairosDBStore kairosDbStore;
    private DataServiceMetrics metrics;
    private WorkerResult wrMock;

    @Before
    public void setUp() {
        metrics = Mockito.mock(DataServiceMetrics.class);
        Timer timer = Mockito.mock(Timer.class);
        Context context = Mockito.mock(Context.class);
        Mockito.when(timer.time()).thenReturn(context);
        Mockito.when(metrics.getKairosDBTimer()).thenReturn(timer);

        wrMock = Mockito.mock(WorkerResult.class);
        kairosDbStore = Mockito.mock(KairosDBStore.class);
    }

    @After
    public void tearDown() {
        Mockito.reset(wrMock, kairosDbStore, metrics);
    }

    @Test
    public void onEmptyOptional() {
        KairosDbWorkResultWriter writer = new KairosDbWorkResultWriter(kairosDbStore, metrics);
        writer.write(Fixture.writeData(Optional.empty()));
        Mockito.verify(kairosDbStore, Mockito.never()).store(Mockito.any(WorkerResult.class));
        Mockito.verify(metrics, Mockito.never()).markKairosError();
    }

    @Test
    public void nonEmptyOptional() {
        KairosDbWorkResultWriter writer = new KairosDbWorkResultWriter(kairosDbStore, metrics);
        WorkerResult wr = Mockito.mock(WorkerResult.class);
        writer.write(Fixture.writeData(Optional.ofNullable(wr)));
        Mockito.verify(kairosDbStore, Mockito.times(1)).store(Mockito.any(WorkerResult.class));
        Mockito.verify(metrics, Mockito.never()).markKairosError();
    }

    @Test
    public void nonEmptyOptionalWithStoreException() {
        Mockito.doThrow(new RuntimeException("test")).when(kairosDbStore).store(Mockito.any(WorkerResult.class));
        KairosDbWorkResultWriter writer = new KairosDbWorkResultWriter(kairosDbStore, metrics);
        WorkerResult wr = Mockito.mock(WorkerResult.class);
        writer.write(Fixture.writeData(Optional.ofNullable(wr)));
        Mockito.verify(kairosDbStore, Mockito.times(1)).store(Mockito.any(WorkerResult.class));
        Mockito.verify(metrics, Mockito.times(1)).markKairosError();
    }

}
