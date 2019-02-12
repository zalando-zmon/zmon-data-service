package de.zalando.zmon.dataservice.data;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.codahale.metrics.Timer;

import de.zalando.zmon.dataservice.DataServiceMetrics;

import java.util.List;

@Component
public class KairosDbWorkResultWriter extends AbstractWorkResultWriter {

    public static final String KAIROS_WRITER_EXECUTOR = "kairos-writer";
    private final Logger log = LoggerFactory.getLogger(KairosDbWorkResultWriter.class);

    private final DataServiceMetrics metrics;

    private final KairosDBStore kairosStore;

    @Autowired
    KairosDbWorkResultWriter(DataServiceConfigProperties config,
                             KairosDBStore kairosStore,
                             DataServiceMetrics metrics) {
        super(config);
        this.kairosStore = kairosStore;
        this.metrics = metrics;
    }

    @Async(KAIROS_WRITER_EXECUTOR)
    @Override
    protected void store(List<GenericMetrics> genericMetrics) {
        Timer.Context c = metrics.getKairosDBTimer().time();
        try {
            kairosStore.store(genericMetrics);
        } catch (Exception e) {
            metrics.markKairosError();
        } finally {
            c.stop();
        }
    }

}

}
