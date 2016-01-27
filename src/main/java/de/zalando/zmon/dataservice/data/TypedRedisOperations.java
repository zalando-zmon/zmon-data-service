package de.zalando.zmon.dataservice.data;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

/**
 * Wow. Default-implementations in interfaces are a great way to pass around
 * utilities. My first Java-8 experience. ;-)
 * 
 * @author jbellmann
 *
 */
public interface TypedRedisOperations {

    default <K, V> SetOperations<K, V> getSetOperations(RedisOperations operations) {
        return operations.opsForSet();
    }

    default <K, V> ListOperations<K, V> getListOperations(RedisOperations operations) {
        return operations.opsForList();
    }

    default <K, V> ValueOperations<K, V> getValueOperations(RedisOperations operations) {
        return operations.opsForValue();
    }

    default <K, HK, HV> HashOperations<K, HK, HV> getHashOperations(RedisOperations operations) {
        return operations.opsForHash();
    }

    default <K, V> RedisOperations<K, V> getEvalOperations(RedisOperations operations) {
        return operations;
    }

}
