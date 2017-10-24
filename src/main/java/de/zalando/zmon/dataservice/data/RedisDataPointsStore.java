package de.zalando.zmon.dataservice.data;

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

    public void store(String query) {
        try (Jedis jedis = pool.getResource()) {
            jedis.lpush(DATAPOINTS_QUEUE, query);
        }
    }
}
