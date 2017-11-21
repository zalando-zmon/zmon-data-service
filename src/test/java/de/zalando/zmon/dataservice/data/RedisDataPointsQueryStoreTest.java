package de.zalando.zmon.dataservice.data;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RedisDataPointsQueryStoreTest {
    private String json;

    @Before
    public void setUp() throws Exception {
        json = IOUtils.toString(this.getClass().getResourceAsStream("kairosdbQuery.json"), "UTF-8");
    }

    @Test
    public void testCompression() throws Exception {
        final RedisDataPointsQueryStore store = new RedisDataPointsQueryStore(mock(JedisPool.class));
        final byte[] got = store.compress(json);
        assertNotNull(got);
        assertTrue(got.length > 0);
        assertTrue(got.length < json.length()); // a bit flaky, but given the known input, it's fair to expect this
    }

    @Test
    public void testStoreCompressed() throws IOException {
        final JedisPool pool = mock(JedisPool.class);
        final Jedis jedis = mock(Jedis.class);
        when(pool.getResource()).thenReturn(jedis);
        when(jedis.lpush(any(byte[].class), any(byte[].class))).thenReturn(42L);
        final RedisDataPointsQueryStore dataPointsQueryStore = spy(new RedisDataPointsQueryStore(pool));
        final int got = dataPointsQueryStore.store(json);
        assertTrue(got == 0);
        verify(jedis).lpush(any(byte[].class), any(byte[].class));
        verify(dataPointsQueryStore).compress(anyString());
    }

    @Test
    public void testFailedRedisWrite() {
        final JedisPool pool = mock(JedisPool.class);
        final Jedis jedis = mock(Jedis.class);
        when(pool.getResource()).thenReturn(jedis);
        when(jedis.lpush(any(byte[].class), any(byte[].class))).thenThrow(new RuntimeException("alles kapput"));
        final RedisDataPointsQueryStore dataPointsQueryStore = new RedisDataPointsQueryStore(pool);
        final int got = dataPointsQueryStore.store(json);
        assertTrue(got == 1);
    }

    @Test
    public void testFailedCompression() throws IOException {
        final JedisPool pool = mock(JedisPool.class);
        final Jedis jedis = mock(Jedis.class);
        when(pool.getResource()).thenReturn(jedis);
        final RedisDataPointsQueryStore dataPointsQueryStore = spy(new RedisDataPointsQueryStore(pool));
        when(dataPointsQueryStore.compress(anyString())).thenThrow(new IOException("alles kapput"));
        final int got = dataPointsQueryStore.store(json);
        assertTrue(got == 1);
    }
}