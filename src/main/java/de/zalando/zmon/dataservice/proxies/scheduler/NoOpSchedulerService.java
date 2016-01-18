package de.zalando.zmon.dataservice.proxies.scheduler;

/**
 * 
 * @author jbellmann
 *
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

}
