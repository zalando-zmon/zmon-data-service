package de.zalando.zmon.dataservice.data;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by jmussler on 4/22/15.
 */
public class WorkerResult {
    public String account;
    public String team;
    public List<CheckData> results = new LinkedList<>();

    @Override
    public String toString() {
        return "WorkerResult{" +
                "account='" + account + '\'' +
                ", team='" + team + '\'' +
                ", " + (results == null || results.isEmpty() ? "no results" : results.size() + " result(s)") + "}";
    }

    public void formatTimeSeriesMetrics(){
        for (CheckData checkData: this.results) {
            checkData.formatTimeSeriesMetrics();
        }
    }
}
