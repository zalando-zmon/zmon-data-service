package de.zalando.zmon.dataservice.restmetrics;

import java.util.*;

/**
 * Created by jmussler on 05.12.15.
 */
public class ApplicationVersion {

    protected String applicationId;
    protected String applicationVersion;

    protected List<ServiceInstance> instances = new ArrayList<>(4);

    private static int N = 120;

    public ApplicationVersion(String applicationId, String applicationVersion) {
        this.applicationId = applicationId;
        this.applicationVersion = applicationVersion;
    }

    public VersionResult getData(long maxTs) {
        VersionResult result = new VersionResult();

        Set<String> eps = new HashSet<>();
        Set<Integer> codes = new HashSet<>();

        for(ServiceInstance i : instances) {
            for(Endpoint e : i.endpoints) {
                eps.add(e.path + "|" + e.method);
                for(DataSeries d : e.series) {
                    codes.add(d.statusCode);
                }
            }
        }

        for(String ep : eps) {
            EpResult epr = new EpResult();
            result.endpoints.put(ep, epr);

            for(int code : codes) {

                List<DataSeries> series = new ArrayList<>();
                for(ServiceInstance i : instances) {
                    for(Endpoint e : i.endpoints) {
                        if(!ep.equals(e.path+"|"+e.method)) {
                            continue;
                        }
                        for(DataSeries d : e.series) {
                            if(!(d.statusCode==code)) {
                                continue;
                            }
                            series.add(d);
                        }
                    }
                }

                List<EpPoint> points = new ArrayList<>(120);

                for(int i = 0 ; i<N; ++i) {
                    double rate = 0;
                    double latency = 0;
                    double maxLatency = 0;
                    double maxRate = 0;
                    long tsMax = 0;
                    int n = 0;
                    boolean partial = false;

                    for (DataSeries s : series) {
                        // assume that the TS is written and thus up to date, otherwise data point is invalid
                        if(s.ts[i]>(maxTs - (N*60000))) {
                            rate += s.points[i][0];
                            latency += s.points[i][1];
                            tsMax = Math.max(tsMax, s.ts[i]);

                            maxLatency = Math.max(s.points[i][1], maxLatency);
                            maxRate = Math.max(s.points[i][0], maxRate);

                            n++;
                        }
                        else {
                            partial = true;
                        }
                    }
                    points.add(new EpPoint(tsMax, rate, latency / n, maxRate, maxLatency, partial));
                }
                epr.points.put(code, points);
            }
        }

        return result;
    }

    /* for now assume no concurrency issue on instance level here, as freq too low and data arrives per instance */
    public void addDataPoint(String id, String path, String method, int status, long ts, double rate, double latency) {
        ServiceInstance instance = null;
        for(ServiceInstance si : instances) {
            if(si.instanceId.equals(id)) {
                instance = si;
            }
        }
        if(null==instance) {
            instance = new ServiceInstance(id);
            synchronized (this) {
                instances.add(instance);
            }
        }

        Endpoint ep = null;
        for(Endpoint e : instance.endpoints) {
            if(e.path.equals(path) && e.method.equals(method)) {
                ep = e;
            }
        }
        if(null==ep) {
            ep = new Endpoint(path, method);
            instance.endpoints.add(ep);
        }

        DataSeries series = null;
        for(DataSeries ds : ep.series) {
            if(ds.statusCode == status) {
                series = ds;
            }
        }

        if(null==series) {
            series = new DataSeries(status);
            ep.series.add(series);
        }
        series.newEntry(ts, rate, latency);
    }
}
