package de.zalando.zmon.dataservice.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import de.zalando.zmon.dataservice.DataServiceMetrics;

@Component
class MarkWriter implements WorkResultWriter {

    private final DataServiceMetrics metrics;

    @Autowired
    MarkWriter(DataServiceMetrics metrics) {
        this.metrics = metrics;
    }

    @Async
    @Override
    public void write(WriteData writeData) {
        metrics.markAccount(writeData.getAccountId(), writeData.getData().length());
        metrics.markCheck(writeData.getCheckId(), writeData.getData().length());
    }

}
