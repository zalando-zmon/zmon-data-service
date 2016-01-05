package de.zalando.zmon.dataservice.restmetrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jmussler on 05.12.15.
 */
class ServiceInstance {
    protected final String instanceId;
    protected final List<Endpoint> endpoints = new ArrayList<>();

    public ServiceInstance(String id) {
        instanceId = id;
    }

    // returns an assumed max time stamp, assuming that we have regular wrap arrounds that should be fine
    public long getMaxTimestamp() {
        long ts = 0;
        for(Endpoint ep : endpoints) {
            ts = Math.max(ts, ep.getMaxTimestamp());
        }
        return ts;
    }
}
