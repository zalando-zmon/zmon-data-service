package de.zalando.zmon.dataservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by jmussler on 11/11/15.
 */

@Service
public class RedisMetricsUpdater implements Runnable {
    private JedisPool pool;
    private String name;
    private DataServiceMetrics metrics;

    private ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

    @Autowired
    public RedisMetricsUpdater(DataServiceConfig config, JedisPool pool, DataServiceMetrics metrics) {
        this.pool = pool;
        this.metrics = metrics;
        try {
            name = "d-p"+ config.server_port()+"."+InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            name = "d-p"+ config.server_port()+".unknown_host";
        }

        exec.scheduleAtFixedRate(this, 15, 3, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        try {
            Jedis jedis = pool.getResource();
            try {
                Pipeline p = jedis.pipelined();
                p.sadd("zmon:metrics", name);
                p.set("zmon:metrics:" + name + ":check.count", metrics.getTotalCount() + "");
                p.set("zmon:metrics:" + name + ":ts", System.currentTimeMillis()/1000 + "");
                p.sync();
            }
            finally
            {
                pool.returnResource(jedis);
            }
        }
        catch(Exception ex) {

        }
    }
}
