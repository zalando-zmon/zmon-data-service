package de.zalando.zmon.dataservice.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.codahale.metrics.Timer;

import de.zalando.zmon.dataservice.DataServiceMetrics;

@Component
public class KairosDbWorkResultWriter implements WorkResultWriter {

    public static final String KAIROS_WRITER_EXECUTOR = "kairos-writer";
    private final Logger log = LoggerFactory.getLogger(KairosDbWorkResultWriter.class);

    private final DataServiceMetrics metrics;

    private final KairosDBStore kairosStore;

    @Autowired
    KairosDbWorkResultWriter(KairosDBStore kairosStore, DataServiceMetrics metrics) {
        this.kairosStore = kairosStore;
        this.metrics = metrics;
    }

    @Async(KAIROS_WRITER_EXECUTOR)
    @Override
    public void write(WriteData writeData) {
        log.debug("write to KairosDB ...");
        if (writeData.getWorkerResultOptional().isPresent()) {
            Timer.Context c = metrics.getKairosDBTimer().time();
            try {
                kairosStore.store(writeData.getWorkerResultOptional().get());
                log.debug("... written to KairosDb");
            } catch (Exception e) {
                log.error("failed kairosdb write check={} data={}", writeData.getCheckId(), writeData.getData(), e);
                metrics.markKairosError();
            } finally {
                c.stop();
            }
        }
    }

}
