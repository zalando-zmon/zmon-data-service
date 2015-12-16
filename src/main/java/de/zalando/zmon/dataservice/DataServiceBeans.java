package de.zalando.zmon.dataservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by jmussler on 12/16/15.
 */
@Configuration
public class DataServiceBeans {

    @Bean
    JedisPool getPool(DataServiceConfig config) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setTestOnBorrow(true);
        poolConfig.setMaxTotal(config.getRedis_pool_size());

        return new JedisPool(poolConfig, config.redis_host(), config.redis_port());
    }

    @Bean
    TokenInfoService getTokenInfoService(DataServiceConfig config) {
        return new OAuthTokenInfoService(config.getOauth2_token_info_url());
    }

}
