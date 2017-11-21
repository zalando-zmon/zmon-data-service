package de.zalando.zmon.dataservice.data;

import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Properties;

import static de.zalando.zmon.dataservice.data.RedistTestSupport.REDIS_SERVER;

@Configuration
@Import({RedisAutoConfiguration.class})
class TestConfiguration {
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() throws Exception {
        final PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
        Properties properties = new Properties();
        properties.setProperty("spring.redis.port", Integer.toString(REDIS_SERVER.getPort()));
        pspc.setProperties(properties);
        return pspc;
    }
}
