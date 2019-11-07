package de.zalando.zmon.dataservice;

import com.codahale.metrics.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by jmussler on 4/21/15.
 */

// @Component
public class DataServiceMetrics {

    public static class LastUpdateGauge implements Gauge<Long> {
        private long v = System.currentTimeMillis();

        public void mark() {
            v = System.currentTimeMillis();
        }

        public LastUpdateGauge() {
        }

        @Override
        public Long getValue() {
            return v;
        }
    }

    private final MetricRegistry metrics;

    private final Map<String, Meter> accountRateMeters = new HashMap<>();
    private final Map<String, Meter> accountByteMeters = new HashMap<>();

    private final Map<String, Meter> checkMeters = new HashMap<>();
    private final Map<String, Counter> checkCounter = new HashMap<>();
    private final Map<String, Histogram> checkHistograms = new HashMap<>();

    private final Map<String, Meter> entityMeters = new HashMap<>();

    public final Map<String, LastUpdateGauge> entityLastUpdateStores = new HashMap<>();

    public final Histogram alertDurations;

    private final Meter totalRate;

    public long getTotalCount() {
        return totalRate.getCount();
    }

    private final Meter kairosErrorMeter;
    private final Meter kairosHostErrorMeter;
    private final Meter redisErrorMeter;
    private final Meter parseError;

    private final Meter trialRunDataCount;
    private final Meter trialRunDataError;
    private final Meter kairosDbDataPointsCount;

    private final Timer kairosDBTimer;

    private final Meter proxyErrorMeter;
    private final Meter eventlogErrorMeter;

    private final Meter workerResultsCount;
    private final Meter workerResultsBatchedCount;
    private final Meter workerResultsEmptyCount;

    private final Meter jobMetricsTotal;
    private final Meter jobMetricsIngestionDropped;
    private final Meter jobMetricsIngestionTotal;
    private final Meter nonSampledDropped;

    // @Autowired
    public DataServiceMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
        this.totalRate = metrics.meter("data-service.total-rate");
        this.parseError = metrics.meter("data-service.parse-error");
        this.kairosErrorMeter = metrics.meter("data-service.kairos-errors");
        this.kairosHostErrorMeter = metrics.meter("data-service.kairos-host-errors");
        this.redisErrorMeter = metrics.meter("data-service.redis-errors");
        this.trialRunDataCount = metrics.meter("data-service.trial-run.data");
        this.trialRunDataError = metrics.meter("data-service.trial-run.data.error");
        this.kairosDBTimer = metrics.timer("data-service.kairosdb.timer");
        this.proxyErrorMeter = metrics.meter("data-service.proxy-errors");
        this.eventlogErrorMeter = metrics.meter("data-service.eventlog-errors");
        this.kairosDbDataPointsCount = metrics.meter("data-service.kairosdb-points.written");
        this.alertDurations = metrics.histogram("data-service.alert-durations");
        this.workerResultsCount = metrics.meter("data-service.worker-results");
        this.workerResultsBatchedCount = metrics.meter("data-service.worker-results-batched");
        this.workerResultsEmptyCount = metrics.meter("data-service.worker-results-empty");
        this.jobMetricsTotal = metrics.meter("data-service.job-metrics.total");
        this.jobMetricsIngestionDropped = metrics.meter("data-service.job-metrics-ingestion.dropped");
        this.jobMetricsIngestionTotal = metrics.meter("data-service.job-metrics-ingestion.total");
        this.nonSampledDropped = metrics.meter("data-service.non-sampled.dropped");
    }

    public MetricRegistry getMetricRegistry() {
        return metrics;
    }

    public Meter getOrCreateMeter(Map<String, Meter> meters, String name) {
        Meter m = meters.get(name);
        if (null != m)
            return m;
        synchronized (this) {
            m = meters.get(name);
            if (null != m)
                return m;
            m = metrics.meter(name);
            meters.put(name, m);
            return m;
        }
    }

    public Counter getOrCreateCounter(Map<String, Counter> counters, String name) {
        Counter m = counters.get(name);
        if (null != m)
            return m;
        synchronized (this) {
            m = counters.get(name);
            if (null != m)
                return m;
            m = metrics.counter(name);
            counters.put(name, m);
            return m;
        }
    }

    public Histogram getOrCreateHistogram(Map<String, Histogram> histograms, String name) {
        Histogram m = histograms.get(name);
        if (null != m)
            return m;
        synchronized (this) {
            m = histograms.get(name);
            if (null != m)
                return m;
            m = metrics.histogram(name);
            histograms.put(name, m);
            return m;
        }
    }

    public void markTrialRunError() {
        trialRunDataError.mark();
    }

    public void markTrialRunData() {
        trialRunDataCount.mark();
    }

    public void markRate(long count) {
        totalRate.mark(count);
    }

    public void markParseError() {
        parseError.mark();
    }

    public void markKairosError() {
        kairosErrorMeter.mark();
    }

    public void markKairosHostErrors(long n) {
        kairosErrorMeter.mark(n);
    }

    public void markRedisError() {
        redisErrorMeter.mark();
    }

    public void markAccount(String account, Optional<String> region, int size) {
        if(region.isPresent()) {
            getOrCreateMeter(accountByteMeters, "ds.acc." + account + "." + region.get() + ".check.data-rate").mark(size);
            getOrCreateMeter(accountRateMeters, "ds.acc." + account + "." + region.get() + ".check.check-rate").mark();
        }
        else {
            getOrCreateMeter(accountByteMeters, "ds.acc." + account + ".check.data-rate").mark(size);
            getOrCreateMeter(accountRateMeters, "ds.acc." + account + ".check.check-rate").mark();
        }
    }

    public void markCheck(int checkId, int size) {
        getOrCreateMeter(checkMeters, "ds.check." + checkId + ".rate").mark(size);
        getOrCreateCounter(checkCounter, "ds.check." + checkId + ".counter").inc();
    }

    public void markCriticalCheck(int checkId, String account, int resultSize) {
        final String baseName = "ds.critical.check." + checkId + ".acc." + account;

        getOrCreateMeter(checkMeters, baseName + ".data-rate").mark(resultSize);
        getOrCreateHistogram(checkHistograms, baseName + ".result-size").update(resultSize);
    }

    private void markEntityLastUpdate(String account) {
        LastUpdateGauge g = entityLastUpdateStores.get(account);
        if (null == g) {
            synchronized (this) {
                g = entityLastUpdateStores.get(account);
                if (null == g) {
                    g = new LastUpdateGauge();
                    entityLastUpdateStores.put(account, g);
                    metrics.register("ds.acc." + account + ".entity.lastUpdate", g);
                }
            }
        }
        g.mark();
    }

    public void markEntity(String account, int size) {
        markEntityLastUpdate(account);
        getOrCreateMeter(entityMeters, "ds.acc." + account + ".entity.rate").mark(size);
    }

    public Timer getKairosDBTimer() {
        return kairosDBTimer;
    }

    public void markProxyError() {
        proxyErrorMeter.mark();
    }

    public void markEventLogError() { eventlogErrorMeter.mark(); }

    public void incWorkerResultsCount(long c) {
        workerResultsCount.mark(c);
    }

    public void incWorkerResultsBatchedCount(long c) {
        workerResultsBatchedCount.mark(c);
    }

    public void incWorkerResultsEmptyCount(long c) {
        workerResultsEmptyCount.mark(c);
    }


    public void incKairosDBDataPoints(long c) {
        kairosDbDataPointsCount.mark(c);
    }



    public void updateAlertDurations(long duration) {
        alertDurations.update(duration);
    }

    public void incJobMetricsTotal(long c) { jobMetricsTotal.mark(c);}
    public void incJobMetricsIngestionDropped(long c) { jobMetricsIngestionDropped.mark(c);}
    public void incJobMetricsIngestionTotal(long c) { jobMetricsIngestionTotal.mark(c);}

    public void incNonSampledDropped(long c) { nonSampledDropped.mark(c);}
}
