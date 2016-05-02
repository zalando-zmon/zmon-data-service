package de.zalando.zmon.dataservice.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

public class ApplicationMetricsWriterTest {

    private DataServiceConfigProperties config;

    private AppMetricsClient applicationMetricsClient;

    @Before
    public void setUp() {
        config = new DataServiceConfigProperties();
        List<String> metricHosts = new ArrayList<>();
        metricHosts.add("localhost");
        config.setRestMetricHosts(metricHosts);
        applicationMetricsClient = Mockito.mock(AppMetricsClient.class);
    }

    @After
    public void tearDown() {
        Mockito.reset(applicationMetricsClient);
    }

    @Test
    public void onEmptyOptional() {
        ApplicationMetricsWriter writer = new ApplicationMetricsWriter(applicationMetricsClient, config);
        writer.write(Fixture.writeData(Optional.empty()));
        Mockito.verify(applicationMetricsClient, Mockito.never()).receiveData(Mockito.anyMap());
    }

    @Test
    public void onExistingOptional() {
        ApplicationMetricsWriter writer = new ApplicationMetricsWriter(applicationMetricsClient, config);
        WorkerResult wr = new WorkerResult();
        wr.results.add(new CheckData());
        writer.write(Fixture.writeData(Optional.ofNullable(wr)));
        Mockito.verify(applicationMetricsClient, Mockito.times(1)).receiveData(Mockito.anyMap());
    }

    @Test
    public void onExistingOptionalReceiveDataException() {
        Mockito.doThrow(new RuntimeException("test")).when(applicationMetricsClient).receiveData(Mockito.anyMap());
        ApplicationMetricsWriter writer = new ApplicationMetricsWriter(applicationMetricsClient, config);
        WorkerResult wr = new WorkerResult();
        wr.results.add(new CheckData());
        writer.write(Fixture.writeData(Optional.ofNullable(wr)));
        Mockito.verify(applicationMetricsClient, Mockito.times(1)).receiveData(Mockito.anyMap());
    }

}
