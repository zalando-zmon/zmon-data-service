package de.zalando.zmon.dataservice.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by mabdelhameed on 24/10/2017.
 *
 * Use Redis as a buffer between Data-service and KairosDB.
 *
 */
public class RedisDataPointsStore {
    private static final String DATAPOINTS_QUEUE = "zmon:datapoints";

    private JedisPool pool;

    RedisDataPointsStore(JedisPool pool) {
        this.pool = pool;
    }

    public void store(String query) throws IOException {
        try (Jedis jedis = pool.getResource()) {
            String compressedQuery = compress(query);
            jedis.lpush(DATAPOINTS_QUEUE, compressedQuery);
        }
    }

    private String compress(String str) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DeflaterOutputStream deflater = new DeflaterOutputStream(bytes);

        deflater.write(str.getBytes());
        deflater.flush();
        deflater.close();

        return new String(bytes.toByteArray());
    }
}
