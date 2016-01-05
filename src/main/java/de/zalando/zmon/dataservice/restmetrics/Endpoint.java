package de.zalando.zmon.dataservice.restmetrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jmussler on 05.12.15.
 */
class Endpoint {

    protected final String method;
    protected final String path;
    protected final List<DataSeries> series = new ArrayList<>(8);

    public Endpoint(String path, String method) {
        this.path = path;
        this.method = method;
    }

    // only used for cleanup every n-hours so we return just any value
    public long getMaxTimestamp() {
        long ts = 0;
        for(DataSeries ds : series) {
            ts = Math.max(ds.ts[0], ts);
            ts = Math.max(ds.ts[ds.ts.length/2], ts); // run 2nd time in case is obsolete or errorneous
        }
        return ts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Endpoint endpoint = (Endpoint) o;
        return endpoint.method.equals(method) && endpoint.path.equals(path);
    }

    @Override
    public int hashCode() {
        int result = method != null ? method.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }
}
