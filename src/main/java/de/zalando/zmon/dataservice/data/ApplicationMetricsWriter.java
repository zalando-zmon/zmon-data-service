package de.zalando.zmon.dataservice.data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

@Component
class ApplicationMetricsWriter implements WorkResultWriter {

    private final Logger log = LoggerFactory.getLogger(ApplicationMetricsWriter.class);

    private final AppMetricsClient applicationMetricsClient;

    private final DataServiceConfigProperties config;

    @Autowired
    ApplicationMetricsWriter(AppMetricsClient applicationMetricsClient, DataServiceConfigProperties config) {
        this.applicationMetricsClient = applicationMetricsClient;
        this.config = config;
    }

    @Async
    @Override
    public void write(WriteData writeData) {
        log.debug("write application-metrics ...");
        if (writeData.getWorkerResultOptional().isPresent() && config.getRestMetricHosts()!=null && config.getRestMetricHosts().size()>0) {
            try {
                Map<Integer, List<CheckData>> partitions = writeData.getWorkerResultOptional().get().results.stream()
                        .filter(x -> config.getActuatorMetricChecks().contains(x.check_id)).filter(x -> !x.exception)
                        .collect(Collectors.groupingBy(x -> Math
                                .abs(x.entity.get("application_id").hashCode() % config.getRestMetricHosts().size())));

                applicationMetricsClient.receiveData(partitions);
                log.debug("application-metrics written");
            } catch (Exception ex) {
                // TODO, do we have a metric for this error too
                log.error("Failed to write to REST metrics data={}", writeData.getData(), ex);
            }
        }
    }

}
