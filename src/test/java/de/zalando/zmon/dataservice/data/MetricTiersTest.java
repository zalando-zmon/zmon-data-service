package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import de.zalando.zmon.dataservice.TokenWrapper;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class MetricTiersTest {

    @Test
    public void isMetricEnabled() {
        final MetricTiers metricTiers = new MetricTiers(
                mock(ObjectMapper.class),
                mock(DataServiceConfigProperties.class),
                mock(TokenWrapper.class)
        );

        final Set<Integer> set = ImmutableSet.of(1);

        // max tier is not in (critical, important)
        metricTiers.setIngestMaxCheckTier(0);
        assertTrue(metricTiers.isMetricEnabled(1));
        metricTiers.setIngestMaxCheckTier(3);
        assertTrue(metricTiers.isMetricEnabled(1));

        // max tier = important
        metricTiers.setIngestMaxCheckTier(2);
        assertFalse(metricTiers.isMetricEnabled(1));
        metricTiers.setImportantChecks(set);
        assertTrue(metricTiers.isMetricEnabled(1));

        // max tier = critical
        metricTiers.setIngestMaxCheckTier(1);
        assertFalse(metricTiers.isMetricEnabled(1));
        metricTiers.setCriticalChecks(set);
        assertTrue(metricTiers.isMetricEnabled(1));
    }
}