package de.zalando.zmon.dataservice.data;

import de.zalando.zmon.dataservice.RedisServerRule;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.DefaultScriptExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.Collections;

@ContextConfiguration
public class LuaScriptTest implements TypedRedisOperations, TypedRedisTemplate {

    @Rule
    public SpringMethodRule methodRule = new SpringMethodRule();

    @ClassRule
    public static final SpringClassRule clazzRule = new SpringClassRule();

    @ClassRule
    public static final RedisServerRule redisServerRule = new RedisServerRule();

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Test
    public void putDataWithRedisTemplate() throws InterruptedException {
        DefaultRedisScript<String> redisScript = new DefaultRedisScript<String>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/scripts/simple.lua")));
        redisScript.setResultType(String.class);
        RedisTemplate<String, String> stringTemplate = getSetOperations(redisTemplate);
        DefaultScriptExecutor<String> executor = new DefaultScriptExecutor<>(stringTemplate);
        String result = executor.execute(redisScript, Collections.singletonList("zmon:alert:1"));
        System.out.println(result);
    }

    @Test
    public void countScript() throws InterruptedException {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<Long>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/scripts/count.lua")));
        redisScript.setResultType(Long.class);
        RedisTemplate<String, String> stringTemplate = getSetOperations(redisTemplate);
        DefaultScriptExecutor<String> executor = new DefaultScriptExecutor<>(stringTemplate);
        Long result = executor.execute(redisScript, Collections.singletonList("zmon:alert:1"));
        System.out.println(result);
    }

    @Test
    public void ifScript() throws InterruptedException {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<Long>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/scripts/if.lua")));
        redisScript.setResultType(Long.class);
        RedisTemplate<String, String> stringTemplate = getSetOperations(redisTemplate);
        DefaultScriptExecutor<String> executor = new DefaultScriptExecutor<>(stringTemplate);
        Long result = executor.execute(redisScript, Collections.singletonList("zmon:alert:1"), "1");
        System.out.println(result);
    }

    @Test
    public void checkAlertsScript() throws InterruptedException {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<Long>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/scripts/checkAlerts.lua")));
        redisScript.setResultType(Long.class);
        RedisTemplate<String, String> stringTemplate = getSetOperations(redisTemplate);
        DefaultScriptExecutor<String> executor = new DefaultScriptExecutor<>(stringTemplate);
        Long result = executor.execute(redisScript, Collections.singletonList("zmon:alerts:1"), "1");
        System.out.println(result);
    }

    @Configuration
    @Import({ RedisConfig.class, RedisAutoConfiguration.class })
    static class TestConfiguration {
        @Bean
        public DataServiceConfigProperties dataServiceConfigProperties() {
            DataServiceConfigProperties props = new DataServiceConfigProperties();
            props.setRedisPort(redisServerRule.getPort());
            return props;
        }
    }

}
