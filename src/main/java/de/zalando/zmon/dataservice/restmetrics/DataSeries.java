package de.zalando.zmon.dataservice.restmetrics;

/**
 * Created by jmussler on 05.12.15.
 */
class DataSeries {

    private static int N = 120;

    protected int statusCode;
    protected long[] ts = new long[N];
    protected double[][] points = new double[N][2];

    public DataSeries(int code) {
        statusCode = code;
    }

    // write to array in ring form with index based on time from 0
    public void newEntry(long t, double r, double l) {
        int bucket = (int)((t / 60000) % N);
        ts[bucket] = t;
        points[bucket][0]=r;
        points[bucket][1]=l;
    }
}
