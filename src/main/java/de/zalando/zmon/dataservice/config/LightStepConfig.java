package de.zalando.zmon.dataservice.config;

import io.opentracing.Tracer;
import com.lightstep.tracer.jre.JRETracer;
import com.lightstep.tracer.shared.Options;

import java.net.MalformedURLException;

public class LightStepConfig {
    private String lightStepHost = "localhost";
    private int lightStepPort = 80;
    private String lightStepAccessToken = "";

    public Tracer getTracer() {
        return tracer;
    }

    private Tracer tracer;

    public LightStepConfig(DataServiceConfigProperties config) throws MalformedURLException {
        this.lightStepHost = config.getLightStepHost();
        this.lightStepPort = config.getLightStepPort();
        this.lightStepAccessToken = config.getLightStepAccessToken();

        Options opts = new Options.OptionsBuilder()
                            .withAccessToken(lightStepAccessToken)
                            .withCollectorHost(lightStepHost)
                            .withCollectorPort(lightStepPort)
                            .withComponentName(config.getOpenTracingServiceName())
                            .build();
        this.tracer = new JRETracer(opts);
    }
}
