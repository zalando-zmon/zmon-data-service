package de.zalando.zmon.dataservice.proxies.mobile;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.io.IOException;

import de.zalando.zmon.dataservice.data.TestingProperties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import de.zalando.zmon.dataservice.AbstractControllerTest;
import de.zalando.zmon.dataservice.components.DefaultObjectMapper;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import de.zalando.zmon.dataservice.config.ObjectMapperConfig;

@ContextConfiguration
@DirtiesContext
public class MobileApiProxyTest extends AbstractControllerTest {

    @Rule
    public final WireMockRule wireMockRule = new WireMockRule(9998);

    private MockMvc mockMvc;

    @Autowired
    private DataServiceConfigProperties config;

    @Autowired
    @DefaultObjectMapper
    private ObjectMapper defaultObjectMapper;

    @Before
    public void setUp() throws IOException {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/v1/checks/all-active-alert-definitions")).willReturn(
                aResponse().withStatus(200).withBody(resourceToString(jsonResource("allAlerts"))).withFixedDelay(200)));
        wireMockRule.stubFor(get(urlPathEqualTo("/api/v1/status/alert/13/details"))
                .willReturn(aResponse().withStatus(200).withBody("").withFixedDelay(200)));
        wireMockRule.stubFor(get(urlPathEqualTo("/api/v1/status/active-alerts"))
                .willReturn(aResponse().withStatus(200).withBody("").withFixedDelay(200)));
        wireMockRule.stubFor(get(urlPathEqualTo("/api/v1/status")).willReturn(aResponse().withStatus(200)
                .withBody(resourceToString(jsonResource("zmonStatus"))).withFixedDelay(200)));
        wireMockRule.stubFor(get(urlPathEqualTo("/api/v1/teams")).willReturn(
                aResponse().withStatus(200).withBody(resourceToString(jsonResource("allTeams"))).withFixedDelay(200)));

        this.mockMvc = MockMvcBuilders.standaloneSetup(new MobileApiProxy(config, defaultObjectMapper))
                .alwaysDo(MockMvcResultHandlers.print()).build();
    }

    @Test
    public void getAlertsStups() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/mobile/alert?team=stups"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void getAlert() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/mobile/alert/13"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void getActiveAlerts() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/mobile/active-alerts?team=stups"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void getZmonStatus() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/mobile/status"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void getAllTeams() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/mobile/all-teams"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Override
    protected Resource jsonResource(String filename) {
        return new ClassPathResource(filename + ".json", getClass());
    }

    @Configuration
    @Import({ ObjectMapperConfig.class, TestingProperties.class })
    static class TestConfig {

        public TestConfig(DataServiceConfigProperties props) {
            props.setProxyController(true);
            props.setProxyControllerBaseUrl("http://localhost:9998");
        }
    }
}
