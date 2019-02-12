package de.zalando.zmon.dataservice.data;

import com.codahale.metrics.Timer;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class M3DbWorkResultWriter extends AbstractWorkResultWriter {

    public static final String M3DB_WRITER_EXECUTOR = "m3db-writer";
    private final Logger log = LoggerFactory.getLogger(KairosDbWorkResultWriter.class);

    private final M3DbStore m3DbStore;

    private final DataServiceMetrics metrics;

    @Autowired
    M3DbWorkResultWriter(DataServiceConfigProperties config,
                         M3DbStore m3DbStore,
                         DataServiceMetrics metrics) {
        super(config);
        this.m3DbStore = m3DbStore;
        this.metrics = metrics;
    }

    @Override
    public void store(List<GenericMetrics> genericMetrics) {
        log.debug("Writing to M3DB ...");
        Timer.Context c = metrics.getM3DBTimer().time();
        try {
            m3DbStore.store(genericMetrics);
        } catch (Exception e) {
            metrics.markM3DbError();
        } finally {
            c.stop();
        }
    }
}
