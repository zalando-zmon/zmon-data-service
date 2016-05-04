package de.zalando.zmon.dataservice.config;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

/**
 * Created by jmussler on 04.05.16.
 */
@Configuration
@EnableAsync
public class ZMonAsyncConfigurer implements AsyncConfigurer {

    private final static Logger LOG = LoggerFactory.getLogger(ZMonAsyncConfigurer.class);

    @Autowired
    private DataServiceConfigProperties properties;

    @Autowired
    private MetricRegistry metricRegistry;

    @Bean
    LinkedBlockingQueue<Runnable> taskExecutorQueue() {
        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(properties.getAsyncPoolQueueSize());
        metricRegistry.register(MetricRegistry.name("data-service.async.executor", "queue", "size"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return queue.size();
            }
        });
        //
        return queue;
    }

    @Override
    public Executor getAsyncExecutor() {
        LOG.info("Creating Async Pool with core-size={} max-size={} queue-size={}", properties.getAsyncPoolCoreSize(), properties.getAsyncPoolMaxSize(), properties.getAsyncPoolQueueSize());
        ThreadPoolTaskExecutor executor = new CustomizableThreadPoolTaskExecutor(taskExecutorQueue());
        executor.setCorePoolSize(properties.getAsyncPoolCoreSize());
        executor.setMaxPoolSize(properties.getAsyncPoolMaxSize());

        // will be ignored
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

    static class CustomizableThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

        private static final long serialVersionUID = 1L;

        private final LinkedBlockingQueue<Runnable> queue;

        CustomizableThreadPoolTaskExecutor(LinkedBlockingQueue<Runnable> queue) {
            this.queue = queue;
        }

        @Override
        protected BlockingQueue<Runnable> createQueue(int queueCapacity) {
            if (queue == null) {
                return super.createQueue(queueCapacity);
            } else {
                return queue;
            }
        }
    }
}
