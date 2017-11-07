package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.JsonNode;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import de.zalando.zmon.dataservice.DataServiceMetrics;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static de.zalando.zmon.dataservice.utils.JsonUtils.flatMapJsonNode;
import static de.zalando.zmon.dataservice.utils.JsonUtils.flatMapJsonNumericNodes;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
class MarkWriter implements WorkResultWriter {

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

        metrics.markAccount(writeData.getAccountId(), writeData.getRegion(), writeData.getData().length());

        if(properties.isTrackCheckRate()) {

            List<Map<String, JsonNode>> flatMaps = newArrayList();

            //  This is already done once in the Kairos writer.  Unfortunately this must be done again
            //  here in its entirety as this is the only place in the service we have all the required
            //  ingestion data needed for the metrics we want to track.  Simply counting valid nodes in
            //  the trees is not adequate as it will over count in the case of nested objects whose
            //  squashed names will collide.  The entire maps must be rebuilt:
            if (writeData.getWorkerResultOptional().isPresent() && !isEmpty(writeData.getWorkerResultOptional().get().results)) {
                flatMaps.addAll(writeData.getWorkerResultOptional().get().results.stream()
                        .filter(checkData -> checkData.check_result != null)
                        .map(checkData -> flatMapJsonNode(checkData.check_result.get("value")))
                        .collect(toList()));
            }

            int totalFields = flatMaps.stream()
                    .mapToInt(Map::size)
                    .sum();

            int metricFields = flatMaps.stream()
                    .mapToInt(m -> flatMapJsonNumericNodes(m).size())
                    .sum();

            metrics.markCheck(
                    writeData.getCheckId(), writeData.getData().length(),
                    totalFields, metricFields, writeData.getAccountId(), writeData.getRegion());
        }
    }
}
