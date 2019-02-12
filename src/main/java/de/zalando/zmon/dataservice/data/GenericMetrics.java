package de.zalando.zmon.dataservice.data;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public class GenericMetrics {
    private final Long timeStampLong;
    private final List<GenericDataPoint> dataPoints;
    private final String checkId;

    public static class GenericDataPoint {
        private final String id;
        private final Long value;
        private final Map<String, String> tags;

        public GenericDataPoint(String id, Long value, Map<String, String> tags) {
            this.id = id;
            this.value = value;
            this.tags = tags;
        }

        public String getId() {
            return id;
        }

        public Long getValue() {
            return value;
        }

        public Map<String, String> getTags() {
            return tags;
        }
    }

    public GenericMetrics(final String checkId, final Long timeStampLong) {
        this.checkId = checkId;
        this.timeStampLong = timeStampLong;
        this.dataPoints = Lists.newArrayList();
    }

    public String getCheckId() {
        return checkId;
    }

    public Long getTimeStampLong() {
        return timeStampLong;
    }

    public List<GenericDataPoint> getDataPoints() {
        return dataPoints;
    }
}
