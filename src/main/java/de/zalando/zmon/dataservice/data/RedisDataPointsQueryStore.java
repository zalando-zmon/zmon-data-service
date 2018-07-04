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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
            LOG.debug("Query: " + query);
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
        SpanContext spanContext = tracer.activeSpan().context();
        Carrier carrier = new Carrier();
        tracer.inject(spanContext, Format.Builtin.TEXT_MAP, carrier);

        final byte[] dataToCompress = str.getBytes();
        final byte[] context = carrier.toString().getBytes();
        int context_length = context.length;
        byte[] length = Integer.toString(context_length).getBytes();

        final byte[] spanContextLength = ByteBuffer.allocate(4).putInt(context_length).array();

        for(int i = 0 ; i < spanContextLength.length; i++) {
            LOG.debug("sp lemght : " + spanContextLength[i] + "\n");
        }

        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream(dataToCompress.length);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataToCompress.length + spanContextLength.length + context.length);
        try {
            try (GZIPOutputStream zipStream = new GZIPOutputStream(byteStream, true)) {
                zipStream.write(dataToCompress);

                outputStream.write("0".getBytes());
                outputStream.write(spanContextLength);
                outputStream.write(context);
                outputStream.write(byteStream.toByteArray());
                LOG.debug(byteStream.toString());
                LOG.debug(outputStream.toString());

            }
        } finally {
            byteStream.close();
            outputStream.close();
        }
        return outputStream.toByteArray();
    }
}
