package de.zalando.zmon.dataservice.data;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import de.zalando.zmon.dataservice.DataServiceMetrics;

@Component
class MarkWriter implements WorkResultWriter {

    private static final Logger LOG = LoggerFactory.getLogger(MarkWriter.class);

    private final DataServiceMetrics metrics;
    private final DataServiceConfigProperties properties;

    @Autowired
    MarkWriter(DataServiceConfigProperties properties, DataServiceMetrics metrics) {
        this.metrics = metrics;
        this.properties = properties;
    }

    @Async
    @Override
    public void write(WriteData writeData) {
        LOG.debug("write metrics ...");
        metrics.markAccount(writeData.getAccountId(), writeData.getRegion(), writeData.getData().length());
        if(properties.isTrackCheckRate()) {
            metrics.markCheck(writeData.getCheckId(), writeData.getData().length());
        }
        LOG.debug("metrics written");
    }

}
