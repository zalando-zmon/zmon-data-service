package de.zalando.zmon.dataservice.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import de.zalando.zmon.dataservice.DataServiceMetrics;

@Component
public class RedisWorkerResultWriter implements WorkResultWriter {

    public static final String REDIS_WRITER_EXECUTOR = "redis-writer";
    private final Logger log = LoggerFactory.getLogger(RedisWorkerResultWriter.class);

    private final RedisDataStore redisDataStore;

    private final DataServiceMetrics metrics;

    @Autowired
    RedisWorkerResultWriter(RedisDataStore redisDataStore, DataServiceMetrics metrics) {
        this.redisDataStore = redisDataStore;
        this.metrics = metrics;
    }

    @Async(REDIS_WRITER_EXECUTOR)
    @Override
    public void write(WriteData writeData) {
        log.debug("write to redis ...");
        if (writeData.getWorkerResultOptional().isPresent()) {
            try {
                redisDataStore.store(writeData.getWorkerResultOptional().get());
                log.debug("written to redis");
            } catch (Exception e) {
                log.error("failed redis write check={} data={}", writeData.getCheckId(), writeData.getData(), e);
                metrics.markRedisError();
            }
        }
    }

}
