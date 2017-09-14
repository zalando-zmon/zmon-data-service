package de.zalando.zmon.dataservice.opentracing;

import io.opentracing.Tracer;

public interface OpenTracing {
    public Tracer generateTracer();
}
