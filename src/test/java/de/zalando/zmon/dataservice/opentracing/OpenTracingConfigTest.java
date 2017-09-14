package de.zalando.zmon.dataservice.opentracing;

import de.zalando.zmon.dataservice.config.OpenTracingConfigProperties;
import de.zalando.zmon.dataservice.opentracing.OpenTracingConfig;
import io.opentracing.NoopTracer;
import io.opentracing.Tracer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OpenTracingConfigTest {

    @Mock
    private OpenTracingConfigProperties openTracingConfig;

    @InjectMocks
    private OpenTracingConfig config;

    @Test
    public void shouldReturnNoopTracer() throws Exception {
        Tracer tracer = config.tracer();
        assertThat(tracer, instanceOf(NoopTracer.class));
    }

    @Before
    public void setUp() throws Exception {
        when(openTracingConfig.getServiceName()).thenReturn("Foo");
    }

    @Test
    public void shouldReturnJaegerTracer() throws Exception {
        when(openTracingConfig.getTracingProvider()).thenReturn("JAEGER", "jaeger", "Jaeger");
        when(openTracingConfig.getJaegerMaxQueueSize()).thenReturn(100);
        when(openTracingConfig.getJaegerFlushIntervalMs()).thenReturn(1);
        Tracer tracer = config.tracer();
        assertThat(tracer, instanceOf(com.uber.jaeger.Tracer.class));
    }

    @Test
    public void shouldReturnInstanaTracer(){
        when(openTracingConfig.getTracingProvider()).thenReturn("INSTANA", "instana", "Instana");
        Tracer tracer = config.tracer();
        assertThat(tracer, instanceOf(com.instana.opentracing.InstanaTracer.class));
    }

    @Test
    public void shouldReturnLightstepTracer(){
        when(openTracingConfig.getTracingProvider()).thenReturn("LIGHTSTEP", "lightstep", "Lightstep");
        when(openTracingConfig.getLightStepHost()).thenReturn("localhost");
        when(openTracingConfig.getLightStepPort()).thenReturn(80);
        when(openTracingConfig.getLightStepAccessToken()).thenReturn("sacacd");
        Tracer tracer = config.tracer();
        assertThat(tracer, instanceOf(com.lightstep.tracer.jre.JRETracer.class));
    }
}