package de.zalando.zmon.dataservice;

import java.util.HashMap;
import java.util.Map;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

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

    private final Map<String, Meter> entityMeters = new HashMap<>();

    public final Map<String, LastUpdateGauge> entityLastUpdateStores = new HashMap<>();

    private final Meter totalRate;

    public long getTotalCount() {
        return totalRate.getCount();
    }

    private final Meter kairosErrorMeter;
    private final Meter redisErrorMeter;
    private final Meter parseError;

    private final Meter trialRunDataCount;
    private final Meter trialRunDataError;

    private final Timer kairosDBTimer;

    // @Autowired
    public DataServiceMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
        this.totalRate = metrics.meter("data-service.total-rate");
        this.parseError = metrics.meter("data-service.parse-error");
        this.kairosErrorMeter = metrics.meter("data-service.kairos-errors");
        this.redisErrorMeter = metrics.meter("data-service.redis-errors");
        this.trialRunDataCount = metrics.meter("data-service.trial-run.data");
        this.trialRunDataError = metrics.meter("data-service.trial-run.data.error");
        this.kairosDBTimer = metrics.timer("data-service.kairosdb.timer");
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

    public void markRedisError() {
        redisErrorMeter.mark();
    }

    public void markAccount(String account, int size) {
        getOrCreateMeter(accountByteMeters, "ds.acc." + account + ".check.data-rate").mark(size);
        getOrCreateMeter(accountRateMeters, "ds.acc." + account + ".check.check-rate").mark();
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
}
