package de.zalando.zmon.dataservice.restmetrics;

/**
 * Created by jmussler on 12/7/15.
 */
public class EpPoint {

    public EpPoint(long t, double r, double l, double rMax, double lMax, double lMin, boolean partial) {
        ts = t;
        rate = r;
        latency = l;
        this.partial = partial;
        this.maxRate = rMax;
        this.maxLatency= lMax;
        this.minLatency = lMin;
    }

    public boolean partial;
    public double rate;
    public double maxRate;
    public double latency;
    public double maxLatency;
    public double minLatency;
    public long ts;
}
