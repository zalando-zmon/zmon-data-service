package de.zalando.zmon.dataservice.data;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.zalando.zmon.dataservice.DataServiceMetrics;

public class RedisWorkResultWriterTest {

    private RedisDataStore redisDataStore;
    private DataServiceMetrics metrics;
    private WorkerResult wrMock;

    @Before
    public void setUp() {
        wrMock = Mockito.mock(WorkerResult.class);
        redisDataStore = Mockito.mock(RedisDataStore.class);
        metrics = Mockito.mock(DataServiceMetrics.class);
    }

    @After
    public void tearDown() {
        Mockito.reset(wrMock, redisDataStore, metrics);
    }

    @Test
    public void testOptionalNotPresent() {
        RedisWorkerResultWriter writer = new RedisWorkerResultWriter(redisDataStore, metrics);
        writer.write(Fixture.writeData(Optional.empty()));
        Mockito.verify(redisDataStore, Mockito.never()).store(Mockito.any(WorkerResult.class));
    }

    @Test
    public void testOptionalPresent() {
        RedisWorkerResultWriter writer = new RedisWorkerResultWriter(redisDataStore, metrics);
        writer.write(Fixture.writeData(Optional.of(wrMock)));
        Mockito.verify(redisDataStore, Mockito.times(1)).store(Mockito.any(WorkerResult.class));
    }

    @Test
    public void testOptionalPresentStoreThrowsException() {
        Mockito.doThrow(new RuntimeException("test")).when(redisDataStore).store(Mockito.any(WorkerResult.class));
        RedisWorkerResultWriter writer = new RedisWorkerResultWriter(redisDataStore, metrics);
        writer.write(Fixture.writeData(Optional.of(wrMock)));
        Mockito.verify(redisDataStore, Mockito.times(1)).store(Mockito.any(WorkerResult.class));
        Mockito.verify(metrics, Mockito.times(1)).markRedisError();
    }

}
