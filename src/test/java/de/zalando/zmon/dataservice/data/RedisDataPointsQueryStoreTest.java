package de.zalando.zmon.dataservice.data;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import io.opentracing.noop.NoopTracer;
import io.opentracing.util.GlobalTracer;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
        MockTracer tracer = new MockTracer();
        Span span = tracer.buildSpan("testCompression").start();
        try (Scope scope = tracer.scopeManager().activate(span, false)) {
            final RedisDataPointsQueryStore store = new RedisDataPointsQueryStore(mock(JedisPool.class), GlobalTracer.get());
            final byte[] got = store.compress(json);
            assertNotNull(got);
            assertTrue(got.length > 0);
            assertTrue(got.length < json.length()); // a bit flaky, but given the known input, it's fair to expect this
        }catch (Exception e ){

        }finally {
            span.finish();
        }
    }

    @Test
    public void testStoreCompressed() throws IOException {
        MockTracer tracer = new MockTracer();
        Span span = tracer.buildSpan("testStoreCompressed").start();
        try (Scope scope = tracer.scopeManager().activate(span, false)) {
            final JedisPool pool = mock(JedisPool.class);
            final Jedis jedis = mock(Jedis.class);
            when(pool.getResource()).thenReturn(jedis);
            when(jedis.lpush(any(byte[].class), any(byte[].class))).thenReturn(42L);
            final RedisDataPointsQueryStore dataPointsQueryStore = spy(new RedisDataPointsQueryStore(pool, tracer));
            final int got = dataPointsQueryStore.store(json);
            assertTrue(got == 0);
            verify(jedis).lpush(any(byte[].class), any(byte[].class));
            verify(dataPointsQueryStore).compress(anyString());
        }catch (Exception e ){

        }finally {
            span.finish();
        }
    }

    @Test
    public void testFailedRedisWrite() {
        MockTracer tracer = new MockTracer();
        Span span = tracer.buildSpan("testFailedRedisWrite").start();
        try (Scope scope = tracer.scopeManager().activate(span, false)) {
            final JedisPool pool = mock(JedisPool.class);
            final Jedis jedis = mock(Jedis.class);
            when(pool.getResource()).thenReturn(jedis);
            when(jedis.lpush(any(byte[].class), any(byte[].class))).thenThrow(new RuntimeException("alles kapput"));
            final RedisDataPointsQueryStore dataPointsQueryStore = new RedisDataPointsQueryStore(pool, tracer);
            final int got = dataPointsQueryStore.store(json);
            assertTrue(got == 1);
        }catch (Exception e){

        }finally {
            span.finish();
        }
    }

    @Test
    public void testFailedCompression() throws IOException {
        MockTracer tracer = new MockTracer();
        Span span = tracer.buildSpan("testFailedCompression").start();
        try (Scope scope = tracer.scopeManager().activate(span, false)) {
            final JedisPool pool = mock(JedisPool.class);
            final Jedis jedis = mock(Jedis.class);
            when(pool.getResource()).thenReturn(jedis);
            final RedisDataPointsQueryStore dataPointsQueryStore = spy(new RedisDataPointsQueryStore(pool, tracer));
            when(dataPointsQueryStore.compress(anyString())).thenThrow(new IOException("alles kapput"));
            final int got = dataPointsQueryStore.store(json);
            assertTrue(got == 1);
        }catch (Exception e){

        }finally {
            span.finish();
        }

    }
}