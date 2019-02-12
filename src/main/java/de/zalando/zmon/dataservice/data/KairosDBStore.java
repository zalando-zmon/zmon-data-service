package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.google.common.collect.ImmutableSet;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;


/**
 * Created by jmussler on 5/8/15.
 */
@Service
public class KairosDBStore {

    private static final Logger LOG = LoggerFactory.getLogger(KairosDBStore.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final DataServiceConfigProperties config;

    private final DataPointsQueryStore dataPointsQueryStore;

    private final Set<String> entityTagFields;
    // adding alias,account_alias,cluster_alias due to legacy, and should be exclusive anyways
    private final static Set<String> DEFAULT_ENTITY_TAG_FIELDS = new HashSet<>(
            ImmutableSet.of("application_id", "application_version", "stack_name", "stack_version", "application",
                    "version", "account_alias", "cluster_alias", "alias", "namespace"));

    private static final String REPLACE_CHAR = "_";
    private static final Pattern KAIROSDB_INVALID_TAG_CHARS = Pattern.compile("[?@:=\\[\\]]");

    private final DataServiceMetrics metrics;
    private final int resultSizeWarning;

    private static class DataPoint {
        public String name;
        public List<ArrayNode> datapoints = new LinkedList<>();
        public Map<String, String> tags = new HashMap<>();
    }

    @Autowired
    public KairosDBStore(DataServiceConfigProperties config,
                         DataServiceMetrics metrics,
                         DataPointsQueryStore dataPointsQueryStore) {
        this.metrics = metrics;
        this.config = config;
        this.dataPointsQueryStore = dataPointsQueryStore;
        this.resultSizeWarning = config.getResultSizeWarning();

        if (null == config.getKairosdbTagFields() || config.getKairosdbTagFields().size() == 0) {
            this.entityTagFields = DEFAULT_ENTITY_TAG_FIELDS;
        } else {
            this.entityTagFields = new HashSet<>(config.getKairosdbTagFields());
        }
    }


    void store(List<GenericMetrics> genericMetrics) {
        if (!config.isKairosdbEnabled()) {
            return;
        }

        if (genericMetrics == null || genericMetrics.isEmpty()) {
            LOG.warn("Received a request with invalid results: {}", genericMetrics);
            return;
        }

        try {
            List<DataPoint> points = new LinkedList<>();
            for (GenericMetrics m : genericMetrics) {

                for (GenericMetrics.GenericDataPoint dp : m.getDataPoints()) {
                    DataPoint p = new DataPoint();
                    p.name = dp.getId();

                    ArrayNode arrayNode = mapper.createArrayNode();
                    arrayNode.add(m.getTimeStampLong());
                    arrayNode.add(dp.getValue());

                    p.datapoints.add(arrayNode);
                    points.add(p);
                }

                if (points.size() > resultSizeWarning) {
                    // TODO entityId
                    LOG.warn("result size warning: check={} data-points={} entity={}", m.getCheckId(), points.size());
                }
            }

            if (points.size() > 0) {
                metrics.incKairosDBDataPoints(points.size());

                String query = mapper.writeValueAsString(points);
                if (config.isLogKairosdbRequests()) {
                    LOG.info("KairosDB Query: {}", query);
                }

                // Store datapoints query!
                int err = dataPointsQueryStore.store(query);
                if (err > 0) {
                    metrics.markKairosHostErrors(err);
                }
            }

        } catch (IOException ex) {
            if (config.isLogKairosdbErrors()) {
                LOG.error("KairosDB write path failed", ex);
            }
            metrics.markKairosError();
        }
    }
}
