package de.zalando.zmon.dataservice.data;

import java.util.Optional;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.zalando.zmon.dataservice.DataServiceMetrics;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

public class KairosDbStoreMetricsTest {

    private DataServiceMetrics metrics;
    private DataServiceConfigProperties properties;

    @Before
    public void setUp() {
        metrics = Mockito.mock(DataServiceMetrics.class);
        properties = new DataServiceConfigProperties();
        properties.setKairosdbWriteUrls(newArrayList());
        properties.setTrackCheckRate(true);
    }

    @After
    public void tearDown() {
        Mockito.reset(metrics);
    }

    @Test
    public void markWhenOptionalIsEmpty() {
        KairosDBStore writer = new KairosDBStore(properties, metrics);
        writer.store(Fixture.writeData(Optional.of(Fixture.buildWorkerResult())));
        verify();
    }

    @Test
    public void markWhenOptionalIsNotEmpty() {
        KairosDBStore writer = new KairosDBStore(properties, metrics);
        writer.store(Fixture.writeData(Optional.of(Fixture.buildWorkerResult())));
        verify();
    }

    protected void verify() {
        Mockito.verify(metrics, Mockito.times(5))
                .markFieldsIngested(anyString(), anyObject(), anyInt(), anyInt());
    }
}
