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
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;


/**
 * Created by mabdelhameed on 24/10/2017.
 *
 * Use Redis cluster as a buffer between Data-service and KairosDB.
 *
 */
public class RedisDataPointsQueryStore implements DataPointsQueryStore {
    private static final String DATAPOINTS_QUEUE = "zmon:datapoints";

    private JedisCluster cluster;

    @Autowired
    RedisDataPointsQueryStore(DataServiceConfigProperties config) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setTestOnBorrow(true);
        poolConfig.setMaxTotal(config.getDatapointsRedisPoolSize());

        HostAndPort node = new HostAndPort(config.getDatapointsRedisHost(), config.getDatapointsRedisPort());
        this.cluster = new JedisCluster(node, poolConfig);
    }

    public int store(String query) {
        int error_count = 0;

        try {
            String compressedQuery = compress(query);
            cluster.lpush(DATAPOINTS_QUEUE, compressedQuery);
        } catch (IOException ex) {
            error_count = 1;
        }

        return error_count;
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
