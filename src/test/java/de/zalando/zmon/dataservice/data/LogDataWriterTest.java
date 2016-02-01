package de.zalando.zmon.dataservice.data;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.env.MockEnvironment;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

public class LogDataWriterTest {

    private LogDataWriter writer;
    private DataServiceConfigProperties config;
    private LogDataWriter spy;

    @Before
    public void setUp() {
        config = new DataServiceConfigProperties(new MockEnvironment());
        writer = new LogDataWriter(config);
        spy = Mockito.spy(writer);
    }

    @After
    public void tearDown() {
        Mockito.reset(spy);
    }

    @Test
    public void logginNotEnabled() {
        spy.write(Fixture.writeData(Optional.empty()));
        Mockito.verify(spy, Mockito.never()).logData(Mockito.anyString());
    }

    @Test
    public void logginEnabled() {
        config.setLogCheckData(true);
        spy.write(Fixture.writeData(Optional.empty()));
        Mockito.verify(spy, Mockito.times(1)).logData(Mockito.anyString());
    }

}
