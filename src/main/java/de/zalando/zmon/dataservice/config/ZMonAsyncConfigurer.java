package de.zalando.zmon.dataservice.config;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import de.zalando.zmon.dataservice.data.KairosDbWorkResultWriter;
import de.zalando.zmon.dataservice.data.RedisWorkerResultWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

/**
 * Created by jmussler on 04.05.16.
 */
@Configuration
@EnableAsync
public class ZMonAsyncConfigurer extends AsyncConfigurerSupport {

    private final static Logger LOG = LoggerFactory.getLogger(ZMonAsyncConfigurer.class);

    private final DataServiceConfigProperties properties;
    private final MetricRegistry metricRegistry;

    public ZMonAsyncConfigurer(final DataServiceConfigProperties properties, final MetricRegistry metricRegistry) {
        this.properties = properties;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public Executor getAsyncExecutor() {
        return createExecutor("default");
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    @Bean(RedisWorkerResultWriter.REDIS_WRITER_EXECUTOR)
    public Executor redisWriterExecutor() {
        return createExecutor("redis");
    }

    @Bean(KairosDbWorkResultWriter.KAIROS_WRITER_EXECUTOR)
    public Executor kairosWriterExecutor() {
        return createExecutor("kairos");
    }

    private Executor createExecutor(final String name) {
        final AsyncExecutorProperties config = properties.getAsyncExecutors().getOrDefault(name, AsyncExecutorProperties.DEFAULT);
        final int queueSize = config.getQueueSize();
        final int coreSize = config.getCoreSize();
        final int maxSize = config.getMaxSize();

        LOG.info("Creating Async executor={} with core-size={} max-size={} queue-size={}", name, coreSize, maxSize, queueSize);
        final ThreadPoolTaskExecutor executor = new CustomizableThreadPoolTaskExecutor(taskExecutorQueue(queueSize, name));
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(queueSize); // will be ignored
        executor.setThreadNamePrefix(String.format("zmon-async-%s-", name));
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.initialize();
        return executor;
    }

    private LinkedBlockingQueue<Runnable> taskExecutorQueue(final int capacity, final String name) {
        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(capacity);
        metricRegistry.register(MetricRegistry.name("data-service.async.executor." + name, "queue", "size"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return queue.size();
            }
        });
        return queue;
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
