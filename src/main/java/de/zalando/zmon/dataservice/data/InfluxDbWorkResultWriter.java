package de.zalando.zmon.dataservice.data;

import com.codahale.metrics.Timer;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author raparida on 15.02.19
 */
@Component
public class InfluxDbWorkResultWriter extends AbstractWorkResultWriter {

    public static final String INFLUXDB_WRITER_EXECUTOR = "influxdb-writer";
    private final Logger log = LoggerFactory.getLogger(InfluxDbWorkResultWriter.class);

    private final M3DbStore influxDbStore;

    private final DataServiceMetrics metrics;

    @Autowired
    InfluxDbWorkResultWriter(DataServiceConfigProperties config,
                         M3DbStore influxDbStore,
                         DataServiceMetrics metrics) {
        super(config, metrics);
        this.influxDbStore = influxDbStore;
        this.metrics = metrics;
    }

    @Async(INFLUXDB_WRITER_EXECUTOR)
    @Override
    public void store(List<GenericMetrics> genericMetrics) {
        log.debug("Writing to InfluxDB ...");
        Timer.Context c = metrics.getM3DBTimer().time();
        try {
            influxDbStore.store(genericMetrics);
        } catch (Exception e) {
            metrics.markM3DbError();
        } finally {
            c.stop();
        }
    }
}
