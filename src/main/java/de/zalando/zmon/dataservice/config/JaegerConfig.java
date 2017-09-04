package de.zalando.zmon.dataservice.config;

import com.uber.jaeger.Configuration;
import com.uber.jaeger.samplers.ProbabilisticSampler;
import io.opentracing.Tracer;

public class JaegerConfig {
    private String jaegerHost = "localhost";
    private int jaegerPort = 5775;
    private boolean logSpans = true;
    private int flushIntervalMs = 10;
    private int maxQueueSize= 100;
    private Tracer tracer;

    public Tracer getTracer() {
        return tracer;
    }

    public JaegerConfig(DataServiceConfigProperties config){
        this.jaegerHost=config.getJaegerHost();
        this.jaegerPort=config.getJaegerPort();
        this.logSpans=config.isJaegerLogSpans();
        this.flushIntervalMs=config.getJaegerFlushIntervalMs();
        this.maxQueueSize=config.getJaegerMaxQueueSize();

        this.tracer = new Configuration(config.getOpenTracingServiceName(),
                                    new Configuration.SamplerConfiguration(ProbabilisticSampler.TYPE, 1),
                                    new Configuration.ReporterConfiguration(logSpans, jaegerHost, jaegerPort, flushIntervalMs, maxQueueSize)).getTracer();
    }
}
