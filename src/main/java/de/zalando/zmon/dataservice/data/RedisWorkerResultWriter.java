package de.zalando.zmon.dataservice.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import de.zalando.zmon.dataservice.DataServiceMetrics;

@Component
class RedisWorkerResultWriter implements WorkResultWriter {

    private final Logger log = LoggerFactory.getLogger(RedisWorkerResultWriter.class);

    private final RedisDataStore redisDataStore;

    private final DataServiceMetrics metrics;

    @Autowired
    RedisWorkerResultWriter(RedisDataStore redisDataStore, DataServiceMetrics metrics) {
        this.redisDataStore = redisDataStore;
        this.metrics = metrics;
    }

    @Async
    @Override
    public void write(WriteData writeData) {
        if (writeData.getWorkerResultOptional().isPresent()) {
            try {
                redisDataStore.store(writeData.getWorkerResultOptional().get());
            } catch (Exception e) {
                log.error("failed redis write check={} data={}", writeData.getCheckId(), writeData.getData(), e);
                metrics.markRedisError();
            }
        }
    }

}