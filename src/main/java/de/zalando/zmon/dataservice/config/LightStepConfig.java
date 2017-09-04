package de.zalando.zmon.dataservice.config;

import io.opentracing.Tracer;
import com.lightstep.tracer.jre.JRETracer;
import com.lightstep.tracer.shared.Options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;

public class LightStepConfig implements OpenTracerConfig {

    private String lightStepHost = "localhost";
    private int lightStepPort = 80;
    private String lightStepAccessToken = "";
    private String serviceName;

    private final Logger logger = LoggerFactory.getLogger(OpenTracerConfig.class);

    public Tracer generateTracer() {
        Options opts = null;
        try {
            opts = new Options.OptionsBuilder()
                    .withAccessToken(lightStepAccessToken)
                    .withCollectorHost(lightStepHost)
                    .withCollectorPort(lightStepPort)
                    .withComponentName(serviceName)
                    .build();
        } catch (MalformedURLException e) {
            logger.error("Lightstep host/port configuration incorrect. LightStep host used:" + lightStepHost + " LightStep port used:" + lightStepPort, e.getMessage());
        }
        return new JRETracer(opts);
    }

    public LightStepConfig(DataServiceConfigProperties config)  {
        this.lightStepHost = config.getLightStepHost();
        this.lightStepPort = config.getLightStepPort();
        this.lightStepAccessToken = config.getLightStepAccessToken();
        this.serviceName = config.getOpenTracingServiceName();
    }
}
