package de.zalando.zmon.dataservice.config;

import io.opentracing.Tracer;

public interface OpenTracerConfig {
    public Tracer generateTracer();
}
