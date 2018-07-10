package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapInjectAdapter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import sun.rmi.runtime.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import static org.slf4j.LoggerFactory.getLogger;


enum SpanContextFormat{TEXTMAP,BINARY}

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

        if (tracePayload == null){
            final ByteArrayOutputStream byteStream = new ByteArrayOutputStream(dataToCompress.length);
            try {
                try (GZIPOutputStream zipStream = new GZIPOutputStream(byteStream, true)) {
                    zipStream.write(dataToCompress);
                }
            } finally {
                byteStream.close();
            }
            return byteStream.toByteArray();
        }

        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream(dataToCompress.length);
        ByteArrayOutputStream payLoad = new ByteArrayOutputStream(tracePayload.length + dataToCompress.length);
        try {
            try (GZIPOutputStream zipStream = new GZIPOutputStream(byteStream, true)) {
                zipStream.write(dataToCompress);
                zipStream.close();
                payLoad.write(tracePayload);
                payLoad.write(byteStream.toByteArray());
            }
        } finally {
            byteStream.close();
            payLoad.close();
        }
        return payLoad.toByteArray();
    }

    byte[] buildRedisTracePayload(){

        String sptCtxtFormat = SpanContextFormat.TEXTMAP.toString();
        byte[] context = getSpanContext(sptCtxtFormat);
        if (context == null){
            return null;
        }

        final byte[] firstByte = sptCtxtFormat.equals(SpanContextFormat.TEXTMAP.toString()) ?
                ByteBuffer.allocate(1).put((byte)0).array():
                ByteBuffer.allocate(1).put((byte)1).array();

        int context_length = context.length;
        final byte[] spanContextLength = ByteBuffer.allocate(4).putInt(context_length).array();
        byte[] tracePayload = new byte[firstByte.length + spanContextLength.length + context.length];

        System.arraycopy(firstByte, 0, tracePayload, 0, firstByte.length);
        System.arraycopy(spanContextLength, 0, tracePayload, firstByte.length, spanContextLength.length);
        System.arraycopy(context, 0, tracePayload, firstByte.length + spanContextLength.length, context.length);

        return tracePayload;
    }

    byte[] getSpanContext(String spCtxtFormat){
        SpanContext spanContext = tracer.activeSpan().context();

        if (spCtxtFormat.equals(SpanContextFormat.BINARY.toString())){
            //TODO: Enable the below commented code when the Binary tracer extraction on the Consumer side starts working.
            /*
            //Carrier carrier = new Carrier(new HashMap<>());
            //HashMap <String, String> carrier = new HashMap<String, String>();
            //Spancontext max limit set to 500Bytes
            ByteBuffer buffer = ByteBuffer.allocate(500);
            tracer.inject(spanContext, Format.Builtin.BINARY, buffer);
            byte[] context = new byte[buffer.remaining()];
            buffer.get(context, 0, context.length);
            //byte[] context = buffer.array();*/
            return null;
        }
         Map<String, String> map = new HashMap<>();
         TextMapInjectAdapter carrier = new TextMapInjectAdapter(map);
         tracer.inject(spanContext, Format.Builtin.TEXT_MAP, carrier);
         ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode jsonNode = mapper.convertValue(map, JsonNode.class);
            byte[] context = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode).getBytes();
            return context;
        } catch (JsonProcessingException e) {
            LOG.error("preparing a trace failed" + e.toString());
            return null;
        }
    }
}
