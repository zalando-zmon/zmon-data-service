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

    public Timer getM3DBTimer() {
        return m3DBTimer;
    }

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

    private final Map<String, Meter> entityMeters = new HashMap<>();

    public final Map<String, LastUpdateGauge> entityLastUpdateStores = new HashMap<>();

    public final Histogram alertDurations;

    private final Meter totalRate;

    public long getTotalCount() {
        return totalRate.getCount();
    }

    private final Meter kairosErrorMeter;
    private final Meter kairosHostErrorMeter;
    private final Meter m3DbErrorMeter;
    private final Meter redisErrorMeter;
    private final Meter parseError;

    private final Meter trialRunDataCount;
    private final Meter trialRunDataError;
    private final Meter kairosDbDataPointsCount;
    private final Meter m3DbDataPointsCount;

    private final Timer kairosDBTimer;
    private final Timer m3DBTimer;

    private final Meter proxyErrorMeter;
    private final Meter eventlogErrorMeter;

    // @Autowired
    public DataServiceMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
        this.totalRate = metrics.meter("data-service.total-rate");
        this.parseError = metrics.meter("data-service.parse-error");
        this.kairosErrorMeter = metrics.meter("data-service.kairos-errors");
        this.kairosHostErrorMeter = metrics.meter("data-service.kairos-host-errors");
        this.m3DbErrorMeter = metrics.meter("data-service.kairos-errors");
        this.redisErrorMeter = metrics.meter("data-service.redis-errors");
        this.trialRunDataCount = metrics.meter("data-service.trial-run.data");
        this.trialRunDataError = metrics.meter("data-service.trial-run.data.error");
        this.kairosDBTimer = metrics.timer("data-service.kairosdb.timer");
        this.m3DBTimer = metrics.timer("data-service.m3db.timer");
        this.proxyErrorMeter = metrics.meter("data-service.proxy-errors");
        this.eventlogErrorMeter = metrics.meter("data-service.eventlog-errors");
        this.kairosDbDataPointsCount = metrics.meter("data-service.kairosdb-points.written");
        this.m3DbDataPointsCount = metrics.meter("data-service.m3db-points.written");
        this.alertDurations = metrics.histogram("data-service.alert-durations");
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

    public void markM3DbError() {
        m3DbErrorMeter.mark();
    }

    public void markKairosHostErrors(long n) {
        kairosErrorMeter.mark(n);
    }

    public void markM3DbHostErrors(long n) {
        m3DbErrorMeter.mark(n);
    }

    public void markRedisError() {
        redisErrorMeter.mark();
    }

    public void markAccount(String account, Optional<String> region, int size) {
        if (region.isPresent()) {
            getOrCreateMeter(accountByteMeters, "ds.acc." + account + "." + region.get() + ".check.data-rate").mark(size);
            getOrCreateMeter(accountRateMeters, "ds.acc." + account + "." + region.get() + ".check.check-rate").mark();
        } else {
            getOrCreateMeter(accountByteMeters, "ds.acc." + account + ".check.data-rate").mark(size);
            getOrCreateMeter(accountRateMeters, "ds.acc." + account + ".check.check-rate").mark();
        }
    }

    public void markCheck(int checkId, int size) {
        getOrCreateMeter(checkMeters, "ds.check." + checkId + ".rate").mark(size);
        getOrCreateCounter(checkCounter, "ds.check." + checkId + ".counter").inc();
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

    public void markEventLogError() {
        eventlogErrorMeter.mark();
    }

    public void incKairosDBDataPoints(long c) {
        kairosDbDataPointsCount.mark(c);
    }

    public void incM3DBDataPoints(long c) {
        m3DbDataPointsCount.mark(c);
    }

    public void updateAlertDurations(long duration) {
        alertDurations.update(duration);
    }
}
