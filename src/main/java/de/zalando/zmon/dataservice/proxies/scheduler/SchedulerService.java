package de.zalando.zmon.dataservice.proxies.scheduler;

import java.io.IOException;
import java.net.URISyntaxException;

public interface SchedulerService {

	String trialRun(String dc) throws IOException, URISyntaxException;

	String instantEvaluations(String dc) throws IOException, URISyntaxException;

}
