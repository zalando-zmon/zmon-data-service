package de.zalando.zmon.dataservice.restmetrics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jmussler on 12/7/15.
 */
public class EpResult {
    public String path;
    public String method;

    public EpResult(String p, String m) {
        path = p;
        method = m;
    }
    public Map<Integer, List<EpPoint>> points = new HashMap<>();
}
