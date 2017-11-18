package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.zalando.zmon.dataservice.components.DefaultObjectMapper;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


@Configuration
public class RedisConfig {

    @Bean("redisMainJedisPool")
    JedisPool redisMainJedisPool(DataServiceConfigProperties config) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setTestOnBorrow(true);
        poolConfig.setMaxTotal(config.getRedisPoolSize());

        return new JedisPool(poolConfig, config.getRedisHost(), config.getRedisPort());
    }

    @Bean
    RedisDataStore redisDataStore(@Qualifier("redisMainJedisPool") final JedisPool pool,
                                  @DefaultObjectMapper final ObjectMapper mapper,
                                  final HttpEventLogger eventLogger) {
        return new RedisDataStore(pool, mapper, eventLogger);
    }
}
