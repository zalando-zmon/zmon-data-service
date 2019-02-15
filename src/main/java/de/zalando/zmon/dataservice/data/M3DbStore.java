package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class M3DbStore {
  private static final Logger LOG = LoggerFactory.getLogger(M3DbStore.class);
  private static final ObjectMapper mapper = new ObjectMapper();

  private final DataServiceConfigProperties config;

  private final DataPointsQueryStore dataPointsQueryStore;
  private final DataServiceMetrics metrics;
  private final int resultSizeWarning;

  private static final String M3DB_DEFAULT_NAMESPACE = "default";

  @Autowired
  M3DbStore(
      DataServiceConfigProperties config,
      DataServiceMetrics metrics,
      DataPointsQueryStore dataPointsQueryStore) {
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

  private static class M3DbTag {
    public String name;
    public String value;
  }

  private static class M3DbDataPoint {
    public Long timeStamp;
    public Long value;
  }

  void store(List<GenericMetrics> genericMetrics) {
    if (!config.isM3dbEnabled()) {
      return;
    }

    if (genericMetrics == null || genericMetrics.isEmpty()) {
      LOG.warn("Received a request with invalid results: {}", genericMetrics);
      return;
    }

    try {
      final List<M3DbMetrics> points = new LinkedList<>();
      for (GenericMetrics m : genericMetrics) {

        for (GenericMetrics.GenericDataPoint dp : m.getDataPoints()) {
          M3DbMetrics m3DbMetrics = new M3DbMetrics();
          m3DbMetrics.id = dp.getId();

          // M3Db tag format
          for (Map.Entry<String, String> tag : dp.getTags().entrySet()) {
            M3DbTag m3dbTag = new M3DbTag();
            m3dbTag.name = tag.getKey();
            m3dbTag.value = tag.getValue();
            m3DbMetrics.tags.add(m3dbTag);
          }

          // M3Db data point format
          M3DbDataPoint dataPoint = new M3DbDataPoint();
          dataPoint.timeStamp = m.getTimeStampLong();
          dataPoint.value = dp.getValue();

          m3DbMetrics.dataPoint = dataPoint;
          points.add(m3DbMetrics);
        }
        if (points.size() > resultSizeWarning) {
          // TODO: entityID
          LOG.warn(
              "result size warning: check={} data-points={} entity={}",
              m.getCheckId(),
              points.size());
        }
      }
      if (points.size() > 0) {
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
      if (config.isLogM3dbErrors()) {
        LOG.error("M3Db write path failed", e);
      }
      metrics.markM3DbError();
    }
  }
}
