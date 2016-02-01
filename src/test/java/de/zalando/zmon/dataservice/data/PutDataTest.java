package de.zalando.zmon.dataservice.data;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.zalando.zmon.dataservice.RedisServerRule;
import de.zalando.zmon.dataservice.Resources;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import redis.clients.jedis.JedisPool;

@ContextConfiguration
public class PutDataTest implements Resources {

    @Rule
    public SpringMethodRule methodRule = new SpringMethodRule();

    @ClassRule
    public static final SpringClassRule clazzRule = new SpringClassRule();

    @ClassRule
    public static final RedisServerRule redisServerRule = new RedisServerRule();

    private WorkerResult wr;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Before
    public void setUp() throws IOException {
        wr = mapper.readValue(resourceToString(jsonResource("workerResult")), WorkerResult.class);
    }

    @Test
    public void putDataWithJedisPool() throws InterruptedException {
        RedisDataStore ds = new RedisDataStore(jedisPool, mapper, stringRedisTemplate);
        ds.before(wr);
        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    public void putDataWithRedisTemplate() throws InterruptedException {
        RedisDataStore ds = new RedisDataStore(jedisPool, mapper, stringRedisTemplate);
        ds.after(wr);
        TimeUnit.SECONDS.sleep(10);
    }

    @Configuration
    @Import({ RedisConfig.class, RedisAutoConfiguration.class })
    static class TestConfiguration {

        @Bean
        public DataServiceConfigProperties dataServiceConfigProperties(Environment env) {
            DataServiceConfigProperties props = new DataServiceConfigProperties(env);

            props.setRedisPort(6379);
            return props;
        }
    }

}
