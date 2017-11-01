package de.zalando.zmon.dataservice.data;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

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

        JedisPool pool = new JedisPool(poolConfig, config.getDatapointsRedisHost(), config.getDatapointsRedisPort());

        return new RedisDataPointsStore(pool);
    }
}
