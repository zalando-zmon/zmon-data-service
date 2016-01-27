package de.zalando.zmon.dataservice.proxies.checks;

import org.junit.After;
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
import de.zalando.zmon.dataservice.proxies.checks.ChecksController;
import de.zalando.zmon.dataservice.proxies.checks.ChecksService;


public abstract class AbstractCheckControllerTest extends AbstractControllerTest {

	@Rule
	public final WireMockRule wireMockRule = new WireMockRule(9999);

	protected MockMvc mockMvc;

	@Autowired
	protected ChecksService checksService;

	protected ChecksService spy;

	@Before
	public void setUp() {		
		spy = Mockito.spy(checksService);
		this.mockMvc = MockMvcBuilders.standaloneSetup(new ChecksController(spy))
				.alwaysDo(MockMvcResultHandlers.print()).build();
	}

	@After
	public void tearDown() {
		Mockito.reset(spy);
	}

	@Test
	public void checks() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/checks?query=htg"))
				.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(spy, VerificationModeFactory.atMost(1)).allActiveCheckDefinitions("htg");
	}

	@Test
	public void alerts() throws Exception {
		configureWireMockForAlerts();
		this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/alerts?query=htg"))
				.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(spy, VerificationModeFactory.atMost(1)).allActiveAlertDefinitions("htg");
	}

	protected void configureWireMockForAlerts() {
	}

	@Test
	public void restChecks() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get("/rest/api/v1/checks/all-active-check-definitions?query=htg"))
				.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(spy, VerificationModeFactory.atMost(1)).allActiveCheckDefinitions("htg");
	}

	@Test
	public void restAlerts() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get("/rest/api/v1/checks/all-active-alert-definitions?query=htg"))
				.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(spy, VerificationModeFactory.atMost(1)).allActiveAlertDefinitions("htg");
	}
}
