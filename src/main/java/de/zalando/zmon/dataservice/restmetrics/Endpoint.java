package de.zalando.zmon.dataservice.restmetrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jmussler on 05.12.15.
 */
class Endpoint {
    protected String path;
    protected List<DataSeries> series = new ArrayList<>(8);

    public Endpoint(String path) {
        this.path = path;
    }
}
