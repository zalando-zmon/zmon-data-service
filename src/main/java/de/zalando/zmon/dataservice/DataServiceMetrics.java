package de.zalando.zmon.dataservice;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Created by jmussler on 4/21/15.
 */
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

    private final Map<String, Meter> totalFieldsIngestedMeters = new HashMap<>();
    private final Map<String, Meter> metricsFieldsIngestedMeters = new HashMap<>();

    private final Map<String, Meter> entityMeters = new HashMap<>();

    public final Map<String, LastUpdateGauge> entityLastUpdateStores = new HashMap<>();

    private final Meter totalRate;

    private final Meter kairosErrorMeter;
    private final Meter redisErrorMeter;
    private final Meter parseError;

    private final Meter trialRunDataCount;
    private final Meter trialRunDataError;
    private final Meter kairosDbDataPointsCount;

    private final Timer kairosDBTimer;

    private final Meter proxyErrorMeter;
    private final Meter eventlogErrorMeter;

    public DataServiceMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
        this.totalRate = metrics.meter("data-service.total-rate");
        this.parseError = metrics.meter("data-service.parse-error");
        this.kairosErrorMeter = metrics.meter("data-service.kairos-errors");
        this.redisErrorMeter = metrics.meter("data-service.redis-errors");
        this.trialRunDataCount = metrics.meter("data-service.trial-run.data");
        this.trialRunDataError = metrics.meter("data-service.trial-run.data.error");
        this.kairosDBTimer = metrics.timer("data-service.kairosdb.timer");
        this.proxyErrorMeter = metrics.meter("data-service.proxy-errors");
        this.eventlogErrorMeter = metrics.meter("data-service.eventlog-errors");
        this.kairosDbDataPointsCount = metrics.meter("data-service.kairosdb-points.written");
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

    public void markKairosHostError() {
        kairosErrorMeter.mark();
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

    public void markFieldsIngested(String account, Optional<String> region, int totalFieldsIngested, int metricsFieldsIngested) {

        checkArgument(!isNullOrEmpty(account));
        checkNotNull(region);

        String base;

        if (region.isPresent()) {
            base = String.format("ds.check.%s.%s", region.get(), account);
        } else {
            base = String.format("ds.check.%s", account);
        }

        getOrCreateMeter(totalFieldsIngestedMeters, base + ".total-fields").mark(totalFieldsIngested);
        getOrCreateMeter(metricsFieldsIngestedMeters, base + ".metrics-fields").mark(metricsFieldsIngested);
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

    public void incKairosDBDataPoints(long c) {
        kairosDbDataPointsCount.mark(c);
    }
}
