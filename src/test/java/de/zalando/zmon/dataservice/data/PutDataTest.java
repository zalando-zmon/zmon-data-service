package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.zalando.zmon.dataservice.Resources;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


@RunWith(SpringRunner.class)
@ContextConfiguration
public class PutDataTest extends RedistTestSupport implements Resources {

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
        RedisDataStore ds = new RedisDataStore(jedisPool, mapper, stringRedisTemplate, null);
        ds.store(wr);
        TimeUnit.SECONDS.sleep(10);
    }

    @Configuration
    @Import({ RedisConfig.class, TestConfiguration.class })
    static class Config {
        @Bean
        public DataServiceConfigProperties dataServiceConfigProperties() {
            DataServiceConfigProperties props = new DataServiceConfigProperties();
            props.setRedisPort(REDIS_SERVER.getPort());
            return props;
        }
    }
}
