package de.zalando.zmon.dataservice.data;

import java.io.IOException;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;


@Configuration
public class RedisConfig {

    @Bean
    JedisPool getPool(DataServiceConfigProperties config) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setTestOnBorrow(true);
        poolConfig.setMaxTotal(config.getRedisPoolSize());

        return new JedisPool(poolConfig, config.getRedisHost(), config.getRedisPort());
    }

    @Bean
    RedisDataPointsStore redisDataPointsStore(DataServiceConfigProperties config) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setTestOnBorrow(true);
        poolConfig.setMaxTotal(config.getDatapointsRedisPoolSize());

        HostAndPort node = new HostAndPort(config.getDatapointsRedisHost(), config.getDatapointsRedisPort());
        JedisCluster cluster = new JedisCluster(node, poolConfig);

        return new RedisDataPointsStore(cluster);
    }
}
