package de.zalando.zmon.dataservice.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.zip.DeflaterOutputStream;

import org.springframework.beans.factory.annotation.Autowired;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPool;


/**
 * Created by mabdelhameed on 24/10/2017.
 *
 * Use Redis cluster as a buffer between Data-service and KairosDB.
 *
 */
public class RedisDataPointsQueryStore implements DataPointsQueryStore {
    private static final String DATAPOINTS_QUEUE = "zmon:datapoints";

    private JedisPool pool;

    @Autowired
    RedisDataPointsQueryStore(DataServiceConfigProperties config) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setTestOnBorrow(true);
        poolConfig.setMaxTotal(config.getRedisPoolSize());

        this.pool = new JedisPool(poolConfig, config.getDatapointsRedisHost(), config.getDatapointsRedisPort());
    }

    public int store(String query) {
        int error_count = 0;

        try (Jedis jedis = pool.getResource()){
            jedis.lpush(DATAPOINTS_QUEUE.getBytes(), compress(query));
        } catch (IOException ex) {
            error_count = 1;
        }

        return error_count;
    }

    private byte[] compress(String str) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DeflaterOutputStream deflater = new DeflaterOutputStream(bytes);

        deflater.write(str.getBytes());
        deflater.flush();
        deflater.close();

        return bytes.toByteArray();
    }
}
