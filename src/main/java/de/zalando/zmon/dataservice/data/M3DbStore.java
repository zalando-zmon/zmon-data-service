package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableSet;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public class M3DbStore {
    private static final Logger LOG = LoggerFactory.getLogger(M3DbStore.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final DataServiceConfigProperties config;



    private final DataPointsQueryStore dataPointsQueryStore;
    private final DataServiceMetrics metrics;
    private final int resultSizeWarning;

    private static final String M3DB_DEFAULT_NAMESPACE = "default";

    @Autowired
    M3DbStore(DataPointsQueryStore dataPointsQueryStore, DataServiceMetrics metrics, DataServiceConfigProperties config) {
        this.dataPointsQueryStore = dataPointsQueryStore;
        this.metrics = metrics;
        this.config = config;
        this.resultSizeWarning = config.getResultSizeWarning();
    }

    private static class M3DbMetrics {
        public final String nameSpace = M3DB_DEFAULT_NAMESPACE;
        public String id;
        public List<M3DbTag> tags;
        public M3DbDataPoint dataPoint;
    }
    private static class M3DbTag{
        public String name;
        public String value;
    }
    private static class M3DbDataPoint {
        public Long timeStamp;
        public Long value;
    }

    void store(WorkerResult wr) {
        if (!config.isM3DbEnabled()) {
            return;
        }

        if (wr == null || wr.results == null || wr.results.isEmpty()) {
            LOG.warn("Received a request with invalid results: {}", wr);
            return;
        }

        try {
            final List<M3DbMetrics> points = new LinkedList<>();
            for (CheckData checkData : wr.results) {

                for (CheckData.ZmonTimeSeriesMetrics metrics: checkData.dataPoints) {
                    M3DbMetrics m3DbMetrics = new M3DbMetrics();
                    m3DbMetrics.id = metrics.id;

                    // M3Db tag format
                    for (Map.Entry<String, String> tag: metrics.tags.entrySet()) {
                        M3DbTag m3dbTag = new M3DbTag();
                        m3dbTag.name = tag.getKey();
                        m3dbTag.value = tag.getValue();
                        m3DbMetrics.tags.add(m3dbTag);
                    }

                    // M3Db data point format
                    M3DbDataPoint dataPoint = new M3DbDataPoint();
                    dataPoint.timeStamp = checkData.timeStampLong;
                    dataPoint.value = metrics.value;

                    m3DbMetrics.dataPoint = dataPoint;
                    points.add(m3DbMetrics);

                }
                if (points.size() > resultSizeWarning) {
                    LOG.warn("result size warning: check={} data-points={} entity={}", checkData.checkId, points.size(), checkData.entityId);
                }
            }
            if (points.size()>0){
                metrics.incM3DBDataPoints(points.size());
                String query = mapper.writeValueAsString(points);

                if (config.isLogM3dbRequests()) {
                    LOG.info("M3DB Query: {}", query);
                }

                // Store datapoints query!
                int err = dataPointsQueryStore.store(query);
                if (err > 0) {
                    metrics.markM3DbHostErrors(err);
                }
            }
        } catch (Exception e) {
            if (config.isLogM3dbErrors()){
                LOG.error("M3Db write path failed", e);
            }
            metrics.markM3DbError();
        }
    }
}
