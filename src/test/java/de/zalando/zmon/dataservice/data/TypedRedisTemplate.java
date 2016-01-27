package de.zalando.zmon.dataservice.data;

import org.springframework.data.redis.core.RedisTemplate;

public interface TypedRedisTemplate {

    default <K, V> RedisTemplate<K, V> getSetOperations(RedisTemplate template) {
        return template;
    }

}
