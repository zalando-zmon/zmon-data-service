package de.zalando.zmon.dataservice.opentracing;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.uber.jaeger.Configuration;
import com.uber.jaeger.samplers.*;
import de.zalando.zmon.dataservice.config.OpenTracingConfigProperties;
import io.opentracing.Tracer;

import java.util.Set;

public class JaegerConfig implements OpenTracing {

    private String jaegerHost = "localhost";
    private int jaegerPort = 5775;
    private boolean logSpans = true;
    private int flushIntervalMs = 5000;
    private int maxQueueSize = 100;
    private String serviceName;
    private String samplerType=ProbabilisticSampler.TYPE;
    private final static Set<String> AVAILABLE_SAMPLERS = ImmutableSet.of(
            ConstSampler.TYPE,
            RateLimitingSampler.TYPE,
            RemoteControlledSampler.TYPE,
            ProbabilisticSampler.TYPE);
    private int samplingRate=1;

    public Tracer generateTracer(){
        Tracer tracer = new Configuration(serviceName,
                new Configuration.SamplerConfiguration(samplerType, samplingRate),
                new Configuration.ReporterConfiguration(logSpans, jaegerHost, jaegerPort, flushIntervalMs, maxQueueSize)).getTracer();
        return tracer;
    }

    public JaegerConfig(OpenTracingConfigProperties config){
        this.jaegerHost = config.getJaegerHost();
        this.jaegerPort = config.getJaegerPort();
        this.logSpans = config.isJaegerLogSpans();
        this.flushIntervalMs = config.getJaegerFlushIntervalMs();
        this.maxQueueSize = config.getJaegerMaxQueueSize();
        this.serviceName = config.getServiceName();
        this.samplerType=resolveSamplerType(config.getJaegerSamplerType());
        this.samplingRate=config.getJaegerSamplingRate();
    }

    @VisibleForTesting
    String resolveSamplerType(String configSamplerType) {
        return AVAILABLE_SAMPLERS.stream()
                .filter(s -> configSamplerType != null && configSamplerType.equalsIgnoreCase(s))
                .findFirst()
                .orElse(ProbabilisticSampler.TYPE);
    }
}
