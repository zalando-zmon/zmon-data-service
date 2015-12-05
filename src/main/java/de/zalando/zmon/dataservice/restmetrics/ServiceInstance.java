package de.zalando.zmon.dataservice.restmetrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jmussler on 05.12.15.
 */
class ServiceInstance {
    protected String instanceId;
    protected List<Endpoint> endpoints = new ArrayList<>();

    public ServiceInstance(String id) {
        instanceId = id;
    }
}
