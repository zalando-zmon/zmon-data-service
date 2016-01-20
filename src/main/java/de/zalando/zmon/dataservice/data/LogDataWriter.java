package de.zalando.zmon.dataservice.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

@Component
class LogDataWriter implements WorkResultWriter {

    private final Logger log = LoggerFactory.getLogger(LogDataWriter.class);

    private final DataServiceConfigProperties config;

    @Autowired
    public LogDataWriter(DataServiceConfigProperties config) {
        this.config = config;
    }

    // TODO, can this be async too
    @Override
    public void write(WriteData writeData) {
        if (config.isLogCheckData()) {
            logData(writeData.getData());
        }
    }

    @VisibleForTesting
    protected void logData(String data) {
        log.info("{}", data);
    }

}
