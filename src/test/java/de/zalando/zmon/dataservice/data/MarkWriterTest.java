package de.zalando.zmon.dataservice.data;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.zalando.zmon.dataservice.DataServiceMetrics;

public class MarkWriterTest {
    private DataServiceMetrics metrics;

    @Before
    public void setUp() {
        metrics = Mockito.mock(DataServiceMetrics.class);
    }

    @After
    public void tearDown() {
        Mockito.reset(metrics);
    }

    @Test
    public void markWhenOptionalIsEmpty() {
        MarkWriter writer = new MarkWriter(metrics);
        writer.write(Fixture.writeData(Optional.empty()));
        verify();
    }

    @Test
    public void markWhenOptionalIsNotEmpty() {
        MarkWriter writer = new MarkWriter(metrics);
        WorkerResult wr = Mockito.mock(WorkerResult.class);
        writer.write(Fixture.writeData(Optional.ofNullable(wr)));
        verify();
    }

    protected void verify() {
        Mockito.verify(metrics, Mockito.times(1)).markAccount(Mockito.anyString(), Mockito.anyObject(), Mockito.anyInt());
        Mockito.verify(metrics, Mockito.times(1)).markCheck(Mockito.anyInt(), Mockito.anyInt());
    }

}
