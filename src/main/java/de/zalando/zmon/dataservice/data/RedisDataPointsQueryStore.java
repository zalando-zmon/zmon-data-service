package de.zalando.zmon.dataservice.data;

import com.google.common.annotations.VisibleForTesting;
import de.zalando.zmon.dataservice.opentracing.Carrier;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.util.GlobalTracer;
import jdk.nashorn.internal.objects.Global;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import static org.slf4j.LoggerFactory.getLogger;


/**
 * Use Redis cluster as a buffer between Data-service and KairosDB.
 */
public class RedisDataPointsQueryStore implements DataPointsQueryStore {
    private final Logger LOG = getLogger(RedisDataPointsQueryStore.class);
    private static final byte[] DATAPOINTS_QUEUE = "zmon:datapoints".getBytes();

    private final JedisPool pool;

    @Autowired
    private Tracer tracer;


    RedisDataPointsQueryStore(final JedisPool jedisPool) {
        this.pool = jedisPool;
    }

    RedisDataPointsQueryStore(final JedisPool jedisPool, Tracer tracer) {
        this.pool = jedisPool;
        this.tracer = tracer;
    }

    public int store(String query) {
        try (final Jedis jedis = pool.getResource()) {
            jedis.lpush(DATAPOINTS_QUEUE, compress(query));
            return 0;
        } catch (IOException ex) {
            LOG.error("failed to compress data point query", ex);
        } catch (Exception ex) {
            LOG.error("failed to push data point query to the redis queue", ex);
        }
        return 1;
    }

    @VisibleForTesting
    byte[] compress(String str) throws IOException {

        final byte[] dataToCompress = str.getBytes();
        byte[] tracePayload = buildRedisTracePayload();
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream(dataToCompress.length);
        ByteArrayOutputStream payLoad = new ByteArrayOutputStream(tracePayload.length + dataToCompress.length);

        try {
            try (GZIPOutputStream zipStream = new GZIPOutputStream(byteStream, true)) {
                zipStream.write(dataToCompress);
                payLoad.write(tracePayload);
                payLoad.write(byteStream.toByteArray());
                LOG.debug(byteStream.toString());
                LOG.debug(payLoad.toString());
            }
        } finally {
            byteStream.close();
            payLoad.close();
        }
        return payLoad.toByteArray();
    }

    byte[] buildRedisTracePayload(){

        SpanContext spanContext = tracer.activeSpan().context();
        Carrier carrier = new Carrier(new HashMap<>());
        tracer.inject(spanContext, Format.Builtin.TEXT_MAP, carrier);

        final byte[] firstByte = ByteBuffer.allocate(1).put("0".getBytes()).array();
        final byte[] context = carrier.toString().getBytes();
        int context_length = context.length;
        final byte[] spanContextLength = ByteBuffer.allocate(4).putInt(context_length).array();

        byte[] tracePayload = new byte[firstByte.length + spanContextLength.length + context.length];

        System.arraycopy(firstByte, 0, tracePayload, 0, firstByte.length);
        System.arraycopy(spanContextLength, 0, tracePayload, firstByte.length, spanContextLength.length);
        System.arraycopy(context, 0, tracePayload, firstByte.length + spanContextLength.length, context.length);

        return tracePayload;
    }
}
