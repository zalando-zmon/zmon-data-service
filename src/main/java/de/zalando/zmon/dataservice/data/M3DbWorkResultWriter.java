package de.zalando.zmon.dataservice.data;

import de.zalando.zmon.dataservice.DataServiceMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class M3DbWorkResultWriter implements WorkResultWriter {

    public static final String M3DB_WRITER_EXECUTOR = "m3db-writer";
    private final Logger log = LoggerFactory.getLogger(KairosDbWorkResultWriter.class);

    private final M3DbStore m3DbStore;

    private final DataServiceMetrics metrics;

    @Autowired
    M3DbWorkResultWriter(M3DbStore m3DbStore, DataServiceMetrics metrics) {
        this.m3DbStore = m3DbStore;
        this.metrics = metrics;
    }

    @Override
    public void write(WriteData writeData) {
        log.debug("Writing to M3DB ...");
        m3DbStore.store(writeData.getWorkerResultOptional().get());

    }
}
