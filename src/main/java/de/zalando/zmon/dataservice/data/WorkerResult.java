package de.zalando.zmon.dataservice.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jmussler on 4/22/15.
 */
public class WorkerResult {
    public String account;
    public String team;
    public List<CheckData> results = new ArrayList<CheckData>(0);
}
