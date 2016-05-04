package de.zalando.zmon.dataservice.config;


import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final static Logger LOG = LoggerFactory.getLogger(ZMonAsyncConfigurer.class);

    @Autowired
    private DataServiceConfigProperties properties;

    @Override
    public Executor getAsyncExecutor() {
        LOG.info("Creating Async Pool with core-size={} max-size={} queue-size={}", properties.getAsyncPoolCoreSize(), properties.getAsyncPoolMaxSize(), properties.getAsyncPoolQueueSize());
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getAsyncPoolCoreSize());
        executor.setMaxPoolSize(properties.getAsyncPoolMaxSize());
        executor.setQueueCapacity(properties.getAsyncPoolQueueSize());
        executor.setThreadNamePrefix("zmon-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
