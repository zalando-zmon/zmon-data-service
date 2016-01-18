package de.zalando.zmon.dataservice.proxies.scheduler;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import de.zalando.zmon.dataservice.AbstractControllerTest;
import de.zalando.zmon.dataservice.proxies.scheduler.DataCenterController;
import de.zalando.zmon.dataservice.proxies.scheduler.SchedulerService;

public abstract class AbstractDataCenterControllerTest extends AbstractControllerTest {

	@Rule
	public final WireMockRule wireMockRule = new WireMockRule(9999);

	protected MockMvc mockMvc;

	@Autowired
	protected SchedulerService schedulerProxy;

	protected SchedulerService spy;

	@Before
	public void setUp() {
		spy = Mockito.spy(schedulerProxy);
		this.mockMvc = MockMvcBuilders.standaloneSetup(new DataCenterController(spy))
				.alwaysDo(MockMvcResultHandlers.print()).build();
	}

	@Test
	public void trialRuns() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/trial-runs/htg"))
				.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(spy, VerificationModeFactory.atMost(1)).trialRun("htg");
	}

	@Test
	public void instantEvaluations() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/instant-evaluations/htg"))
				.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(spy, VerificationModeFactory.atMost(1)).instantEvaluations("htg");
	}

}
