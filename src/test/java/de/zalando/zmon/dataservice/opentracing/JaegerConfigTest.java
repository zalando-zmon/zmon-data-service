package de.zalando.zmon.dataservice.opentracing;

import com.uber.jaeger.samplers.ConstSampler;
import com.uber.jaeger.samplers.ProbabilisticSampler;
import com.uber.jaeger.samplers.RateLimitingSampler;
import com.uber.jaeger.samplers.RemoteControlledSampler;
import de.zalando.zmon.dataservice.config.OpenTracingConfigProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JaegerConfigTest {

    @Mock
    private OpenTracingConfigProperties openTracingConfig;

    @Test
    public void shouldReturnProbSampler(){
        when(openTracingConfig.getJaegerSamplerType()).thenReturn("probabilistic", "Probabilistic", "PROBABILISTIC");
        JaegerConfig config = new JaegerConfig(openTracingConfig);
        for (int i=0; i<3; i++) {
            assertEquals(ProbabilisticSampler.TYPE, config.resolveSamplerType(openTracingConfig.getJaegerSamplerType()));
        }
    }

    @Test
    public void shouldReturnConstSampler(){
        when(openTracingConfig.getJaegerSamplerType()).thenReturn("const", "Const", "CONST");
        JaegerConfig config = new JaegerConfig(openTracingConfig);
        for (int i=0; i<3; i++) {
            assertEquals(ConstSampler.TYPE, config.resolveSamplerType(openTracingConfig.getJaegerSamplerType()));
        }
    }

    @Test
    public void shouldReturnRateLimitSampler(){
        when(openTracingConfig.getJaegerSamplerType()).thenReturn("ratelimiting", "Ratelimiting", "RATELIMITING");
        JaegerConfig config = new JaegerConfig(openTracingConfig);
        for (int i=0; i<3; i++) {
            assertEquals(RateLimitingSampler.TYPE, config.resolveSamplerType(openTracingConfig.getJaegerSamplerType()));
        }
    }

    @Test
    public void shouldReturnRemoteControlSampler(){

        when(openTracingConfig.getJaegerSamplerType()).thenReturn("remote", "Remote", "REMOTE");
        JaegerConfig config = new JaegerConfig(openTracingConfig);
        for (int i=0; i<3; i++) {
            assertEquals(RemoteControlledSampler.TYPE, config.resolveSamplerType(openTracingConfig.getJaegerSamplerType()));
        }
    }

}