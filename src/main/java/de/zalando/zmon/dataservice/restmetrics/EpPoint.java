package de.zalando.zmon.dataservice.restmetrics;

/**
 * Created by jmussler on 12/7/15.
 */
public class EpPoint {

    public EpPoint(long t, double r, double l, double rMax, double lMax, boolean partial) {
        ts = t;
        rate = r;
        latency = l;
        this.partial = partial;
        this.maxRate = rMax;
        this.maxLatency= lMax;
    }

    public boolean partial;
    public double rate;
    public double maxRate;
    public double latency;
    public double maxLatency;
    public long ts;
}
