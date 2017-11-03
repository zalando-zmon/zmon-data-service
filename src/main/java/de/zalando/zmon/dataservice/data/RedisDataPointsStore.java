package de.zalando.zmon.dataservice.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

/**
 * Created by mabdelhameed on 24/10/2017.
 *
 * Use Redis cluster as a buffer between Data-service and KairosDB.
 *
 */
public class RedisDataPointsStore {
    private static final String DATAPOINTS_QUEUE = "zmon:datapoints";

    private JedisCluster cluster;

    RedisDataPointsStore(JedisCluster cluster) {
        this.cluster = cluster;
    }

    public void store(String query) throws IOException {
        String compressedQuery = compress(query);
        cluster.lpush(DATAPOINTS_QUEUE, compressedQuery);
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
