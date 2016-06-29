package de.zalando.zmon.dataservice.proxies.scheduler;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author jbellmann
 */
public class NoOpSchedulerService implements SchedulerService {

    @Override
    public String trialRun(String dc) {
        return "";
    }

    @Override
    public String instantEvaluations(String dc) {
        return "";
    }

    @Override
    public String downtimes(String dc) throws IOException, URISyntaxException {
        return "";
    }
}
