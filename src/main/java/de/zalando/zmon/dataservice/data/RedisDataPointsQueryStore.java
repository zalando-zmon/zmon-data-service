package de.zalando.zmon.dataservice.data;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import static org.slf4j.LoggerFactory.getLogger;


/**
 * Use Redis cluster as a buffer between Data-service and KairosDB.
 */
public class RedisDataPointsQueryStore implements DataPointsQueryStore {
    private final Logger LOG = getLogger(RedisDataPointsQueryStore.class);
    private static final byte[] DATAPOINTS_QUEUE = "zmon:datapoints".getBytes();

    private final JedisPool pool;

    RedisDataPointsQueryStore(final JedisPool jedisPool) {
        this.pool = jedisPool;
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
}
