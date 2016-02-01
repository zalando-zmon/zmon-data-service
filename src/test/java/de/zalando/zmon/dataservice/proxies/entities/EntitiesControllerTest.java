package de.zalando.zmon.dataservice.proxies.entities;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import de.zalando.zmon.dataservice.AbstractControllerTest;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import de.zalando.zmon.dataservice.config.ObjectMapperConfig;

@ContextConfiguration
public class EntitiesControllerTest extends AbstractControllerTest {

    @Rule
    public final WireMockRule wireMockRule = new WireMockRule(9998);

    private MockMvc mockMvc;

    @Autowired
    private EntitiesService entitiesService;

    private EntitiesService spy;

    @Before
    public void setUp() throws IOException {
        wireMockRule.stubFor(delete(urlPathEqualTo("/entities/12")).willReturn(aResponse().withStatus(200)));
        wireMockRule.stubFor(get(urlPathEqualTo("/entities"))
                .willReturn(aResponse().withStatus(200).withBody("").withFixedDelay(200)));
        wireMockRule.stubFor(put(urlPathEqualTo("/entities"))
                .willReturn(aResponse().withStatus(200).withBody("").withFixedDelay(200)));

        spy = Mockito.spy(entitiesService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(new EntitiesController(spy))
                .alwaysDo(MockMvcResultHandlers.print()).build();
    }

    @Test
    public void getEntities() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/entities?query=htg"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(spy, VerificationModeFactory.times(1)).getEntities("htg");
    }

    @Test
    public void getEntitiesRest() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/rest/api/v1/entities?query=htg"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(spy, VerificationModeFactory.times(1)).getEntities("htg");
    }

    @Test
    public void addEntities() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/entities").content("true"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(spy, VerificationModeFactory.times(1)).addEntities("true");
    }

    @Test
    public void deleteEntities() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/entities/12"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(spy, VerificationModeFactory.times(1)).deleteEntity("12");
    }

    @Configuration
    @Import({ EntitiesConfig.class, ObjectMapperConfig.class })
    static class TestConfig {

        @Bean
        public DataServiceConfigProperties dataServiceConfigProperties() {
            DataServiceConfigProperties props = new DataServiceConfigProperties();
            props.setProxyController(true);
            props.setProxyControllerUrl("http://localhost:9998");
            return props;
        }

        @Bean
        public DataServiceMetrics dataServiceMetrics() {
            return Mockito.mock(DataServiceMetrics.class);
        }
    }

}
