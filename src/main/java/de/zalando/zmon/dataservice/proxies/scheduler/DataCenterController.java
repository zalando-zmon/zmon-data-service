package de.zalando.zmon.dataservice.proxies.scheduler;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DataCenterController {

	private final SchedulerService schedulerService;

	@Autowired
	public DataCenterController(SchedulerService schedulerService) {
		this.schedulerService = schedulerService;
	}

	@RequestMapping(value = "/trial-runs/{dc}")
	public String getTrialRuns(@PathVariable final String dc) throws IOException, URISyntaxException {

		return schedulerService.trialRun(dc);
	}

	@RequestMapping(value = "/instant-evaluations/{dc}")
	public String getInstantEvals(@PathVariable final String dc) throws IOException, URISyntaxException {

		return schedulerService.instantEvaluations(dc);
	}

}
