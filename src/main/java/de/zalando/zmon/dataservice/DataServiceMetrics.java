package de.zalando.zmon.dataservice;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jmussler on 4/21/15.
 */
@Component
public class DataServiceMetrics {

    private final MetricRegistry metrics;

    private final Map<String, Meter> accountMeters = new HashMap<>();
    private final Map<String, Counter> accountCounter = new HashMap<>();

    private final Map<String, Meter> checkMeters = new HashMap<>();
    private final Map<String, Counter> checkCounter = new HashMap<>();

    private final Meter totalRate;

    @Autowired
    public DataServiceMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
        this.totalRate = metrics.meter("data-service.total-rate");
    }

    public Meter getOrCreateMeter(Map<String, Meter> meters, String name) {
        Meter m = meters.get(name);
        if(null!=m) return m;
        synchronized (this) {
            m = meters.get(name);
            if(null!=m) return m;
            m = metrics.meter(name);
            meters.put(name, m);
            return m;
        }
    }

    public Counter getOrCreateCounter(Map<String, Counter> counters, String name) {
        Counter m = counters.get(name);
        if(null!=m) return m;
        synchronized (this) {
            m = counters.get(name);
            if(null!=m) return m;
            m = metrics.counter(name);
            counters.put(name, m);
            return m;
        }
    }

    public void markRate() {
        totalRate.mark();
    }

    public void markAccount(String account, int size) {
        getOrCreateMeter(accountMeters, "ds.acc."+account+".rate").mark(size);
        getOrCreateCounter(accountCounter, "ds.acc."+account+".counter").inc();
    }

    public void markCheck(int checkId, int size) {
        getOrCreateMeter(checkMeters, "ds.check."+checkId+".rate").mark(size);
        getOrCreateCounter(checkCounter, "ds.check."+checkId+".counter").inc();
    }
}
