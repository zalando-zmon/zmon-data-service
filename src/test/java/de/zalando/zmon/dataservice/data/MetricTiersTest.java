package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import de.zalando.zmon.dataservice.TokenWrapper;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class MetricTiersTest {
    private MetricTiers metricTiers;
    private Random mockedRandom;

    @Before
    public void setUp() {
        mockedRandom = mock(Random.class);
        metricTiers = new MetricTiers(
                mock(ObjectMapper.class),
                mock(DataServiceConfigProperties.class),
                mock(TokenWrapper.class),
                mockedRandom
        );
    }

    @Test
    public void maxTierIsNotCriticalOrImportant() {
        metricTiers.setIngestMaxCheckTier(0);
        assertTrue(metricTiers.isMetricEnabled(1));
        metricTiers.setIngestMaxCheckTier(3);
        assertTrue(metricTiers.isMetricEnabled(1));
    }

    @Test
    public void maxTierIsImportant() {
        metricTiers.setIngestMaxCheckTier(2);
        assertFalse(metricTiers.isMetricEnabled(1));
        metricTiers.setImportantChecks(ImmutableSet.of(1));
        assertTrue(metricTiers.isMetricEnabled(1));
    }

    @Test
    public void maxTierIsCritical() {
        metricTiers.setIngestMaxCheckTier(1);
        assertFalse(metricTiers.isMetricEnabled(1));
        metricTiers.setCriticalChecks(ImmutableSet.of(1));
        assertTrue(metricTiers.isMetricEnabled(1));
    }

    @Test
    public void maxTierIsOtherAndSampledTierIsOther() {
        metricTiers.setSampledCheckRate(0.5);

        metricTiers.setIngestMaxCheckTier(3);
        metricTiers.setSampledCheckTier(3);
        when(mockedRandom.nextDouble()).thenReturn(0.3);
        assertFalse(metricTiers.isMetricEnabled(1));
        when(mockedRandom.nextDouble()).thenReturn(0.7);
        assertTrue(metricTiers.isMetricEnabled(1));
    }

    @Test
    public void maxTierIsImportantAndSampledTierIsOther() {
        metricTiers.setSampledCheckRate(0.5);

        metricTiers.setIngestMaxCheckTier(2);
        metricTiers.setImportantChecks(ImmutableSet.of(1));
        when(mockedRandom.nextDouble()).thenReturn(0.3);
        assertTrue(metricTiers.isMetricEnabled(1)); // random not called here
        assertFalse(metricTiers.isMetricEnabled(2));
    }

    @Test
    public void criticalChecksAreNotSampled() {
        metricTiers.setSampledCheckRate(0.5);
        metricTiers.setCriticalChecks(ImmutableSet.of(1));

        metricTiers.setSampledCheckTier(1);
        assertTrue(metricTiers.isMetricEnabled(1));
        verify(mockedRandom, never()).nextDouble();

        metricTiers.setSampledCheckTier(3);
        assertTrue(metricTiers.isMetricEnabled(1));
        verify(mockedRandom, atMost(1)).nextDouble();
    }
}