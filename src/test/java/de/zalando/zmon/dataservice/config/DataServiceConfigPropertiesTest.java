package de.zalando.zmon.dataservice.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@TestPropertySource(properties = {
        "dataservice.async_executors.foo.core_size=42",
        "dataservice.async_executors.foo.max_size=69",
        "dataservice.async_executors.foo.queue_size=666",
        "dataservice.async_executors.bar.queue_size=333",
})
public class DataServiceConfigPropertiesTest {
    @Configuration
    @EnableConfigurationProperties({DataServiceConfigProperties.class})
    static class Config { }


    @Autowired
    private DataServiceConfigProperties config;

    @Test
    public void testIfDefaultExecutorPropertiesWork() {
        assertThat(config.getAsyncExecutors(), notNullValue());
        final AsyncExecutorProperties properties = config.getAsyncExecutors().getOrDefault("zbr",
                AsyncExecutorProperties.DEFAULT);
        assertThat(properties, is(AsyncExecutorProperties.DEFAULT));
        assertThat(properties.getCoreSize(), is(AsyncExecutorProperties.DEFAULT_CORE_SIZE));
        assertThat(properties.getMaxSize(), is(AsyncExecutorProperties.DEFAULT_MAX_SIZE));
        assertThat(properties.getQueueSize(), is(AsyncExecutorProperties.DEFAULT_QUEUE_SIZE));
    }

    @Test
    public void testFooExecutor() {
        final AsyncExecutorProperties properties = config.getAsyncExecutors().getOrDefault("foo", null);
        assertThat(properties, notNullValue());
        assertThat(properties.getCoreSize(), is(42));
        assertThat(properties.getMaxSize(), is(69));
        assertThat(properties.getQueueSize(), is(666));
    }

    @Test
    public void testBarExecutor() {
        final AsyncExecutorProperties properties = config.getAsyncExecutors().getOrDefault("bar", null);
        assertThat(properties, notNullValue());
        assertThat(properties.getCoreSize(), is(AsyncExecutorProperties.DEFAULT_CORE_SIZE));
        assertThat(properties.getMaxSize(), is(AsyncExecutorProperties.DEFAULT_MAX_SIZE));
        assertThat(properties.getQueueSize(), is(333));
    }
}