package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.zalando.zmon.dataservice.Resources;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import de.zalando.zmon.dataservice.config.ObjectMapperConfig;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import redis.clients.jedis.JedisPool;

import java.io.IOException;

import static org.mockito.Mockito.mock;

@ContextConfiguration
public class PutDataTest extends RedistTestSupport implements Resources {

    @Rule
    public SpringMethodRule methodRule = new SpringMethodRule();

    @ClassRule
    public static final SpringClassRule clazzRule = new SpringClassRule();

    private WorkerResult wr;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private JedisPool jedisPool;

    @Before
    public void setUp() throws IOException {
        wr = mapper.readValue(resourceToString(jsonResource("workerResult")), WorkerResult.class);
    }

    @Test
    public void putDataWithJedisPool() throws InterruptedException {
        RedisDataStore ds = new RedisDataStore(jedisPool, mapper, null);
        ds.store(wr);
    }

    @Configuration
    @Import({RedisConfig.class, TestConfiguration.class, ObjectMapperConfig.class})
    static class Config {
        @Bean
        public DataServiceConfigProperties dataServiceConfigProperties() {
            DataServiceConfigProperties props = new DataServiceConfigProperties();
            props.setRedisPort(REDIS_SERVER.getPort());
            return props;
        }

        @Bean
        public HttpEventLogger eventLogger() {
            return mock(HttpEventLogger.class);
        }
    }

}
