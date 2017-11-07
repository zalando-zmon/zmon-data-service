package de.zalando.zmon.dataservice.data;

import java.util.Optional;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.zalando.zmon.dataservice.DataServiceMetrics;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

public class MarkWriterTest {

    private DataServiceMetrics metrics;
    private DataServiceConfigProperties properties;

    @Before
    public void setUp() {
        metrics = Mockito.mock(DataServiceMetrics.class);
        properties = new DataServiceConfigProperties();
        properties.setTrackCheckRate(true);
    }

    @After
    public void tearDown() {
        Mockito.reset(metrics);
    }

    @Test
    public void markWhenOptionalIsEmpty() {
        MarkWriter writer = new MarkWriter(properties, metrics);
        writer.write(Fixture.writeData(Optional.empty()));
        verify();
    }

    @Test
    public void markWhenOptionalIsNotEmpty() {
        MarkWriter writer = new MarkWriter(properties, metrics);
        WorkerResult wr = Mockito.mock(WorkerResult.class);
        writer.write(Fixture.writeData(Optional.ofNullable(wr)));
        verify();
    }

    protected void verify() {
        Mockito.verify(metrics, Mockito.times(1)).markAccount(anyString(), anyObject(), anyInt());
        Mockito.verify(metrics, Mockito.times(1))
                .markCheck(anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyObject());
    }
}
