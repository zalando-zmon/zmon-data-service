package de.zalando.zmon.dataservice.opentracing;

import com.google.common.annotations.VisibleForTesting;
import com.uber.jaeger.Configuration;
import com.uber.jaeger.samplers.*;
import de.zalando.zmon.dataservice.config.OpenTracingConfigProperties;
import io.opentracing.Tracer;

public class JaegerConfig implements OpenTracing {

    private String jaegerHost = "localhost";
    private int jaegerPort = 5775;
    private boolean logSpans = true;
    private int flushIntervalMs = 5000;
    private int maxQueueSize = 100;
    private String serviceName;
    private String samplerType=ProbabilisticSampler.TYPE;
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
        String samplerType = this.samplerType;
        if (configSamplerType!= null) {
            switch (configSamplerType.toLowerCase()) {
                case ConstSampler.TYPE:
                    samplerType = ConstSampler.TYPE;
                    break;
                case RateLimitingSampler.TYPE:
                    samplerType = RateLimitingSampler.TYPE;
                    break;
                case RemoteControlledSampler.TYPE:
                    samplerType = RemoteControlledSampler.TYPE;
                    break;
            }
        }
        return samplerType;
    }
}
