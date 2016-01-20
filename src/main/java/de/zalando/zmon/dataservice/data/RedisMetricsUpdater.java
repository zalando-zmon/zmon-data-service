package de.zalando.zmon.dataservice.data;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

/**
 * Created by jmussler on 11/11/15.
 */

@Service
public class RedisMetricsUpdater {

    private final Logger log = LoggerFactory.getLogger(RedisMetricsUpdater.class);

    private JedisPool pool;
    private String name;
    private final DataServiceMetrics metrics;

    private final RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    public RedisMetricsUpdater(DataServiceConfigProperties config, DataServiceMetrics metrics,
            RedisTemplate<Object, Object> redisTemplate) {
        // this.pool = pool;
        this.metrics = metrics;
        try {
            name = "d-p" + config.getServerPort() + "." + InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            name = "d-p" + config.getServerPort() + ".unknown_host";
        }

        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedRate = 3000, initialDelay = 15000)
    public void run() {
        log.warn("write scheduled metrics ...");
        // when using 'before()' make sure 'pool' will be assigned in
        // constructor

        // before();
        // after();
        after2();
        log.warn("scheduled metrics written");
    }

    protected void before() {
        try {
            Jedis jedis = pool.getResource();
            try {
                Pipeline p = jedis.pipelined();
                p.sadd("zmon:metrics", name);
                p.set("zmon:metrics:" + name + ":check.count", metrics.getTotalCount() + "");
                p.set("zmon:metrics:" + name + ":ts", System.currentTimeMillis() / 1000 + "");
                p.sync();
            } finally {
                pool.returnResource(jedis);
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    // no resource handling (returnResource()) here, handled by template
    protected void after() {
        try {
            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    ValueOperations<String, String> vOps = getValueOperations(operations);
                    vOps.set("zmon:metrics:" + name + ":check.count", metrics.getTotalCount() + "");
                    vOps.set("zmon:metrics:" + name + ":ts", System.currentTimeMillis() / 1000 + "");
                    return null;
                }
            });
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    protected static ValueOperations<String, String> getValueOperations(RedisOperations operations) {
        return operations.opsForValue();
    }

    // avoid anonymous classes
    private MetricsRedisCallback mcb = new MetricsRedisCallback();

    protected void after2() {
        try {
            redisTemplate.execute(mcb);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    class MetricsRedisCallback implements SessionCallback<Object> {

        @Override
        public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
            ValueOperations<String, String> vOps = getValueOperations(operations);
            vOps.set("zmon:metrics:" + name + ":check.count", metrics.getTotalCount() + "");
            vOps.set("zmon:metrics:" + name + ":ts", System.currentTimeMillis() / 1000 + "");
            return null;
        }

    }
}
