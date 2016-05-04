package de.zalando.zmon.dataservice.config;


import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Created by jmussler on 04.05.16.
 */
@Configuration
@EnableAsync
public class ZMonAsyncConfigurer implements AsyncConfigurer {

    private final DataServiceConfigProperties properties;

    @Autowired
    public ZMonAsyncConfigurer(DataServiceConfigProperties properties) {
        this.properties = properties;
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getAsyncPoolCoreSize());
        executor.setMaxPoolSize(properties.getAsyncPoolMaxSize());
        executor.setQueueCapacity(properties.getAsyncPoolQueueSize());
        executor.setThreadNamePrefix("zmon-async-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
