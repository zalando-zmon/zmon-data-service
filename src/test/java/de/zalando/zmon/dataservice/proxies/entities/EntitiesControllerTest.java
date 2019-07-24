package de.zalando.zmon.dataservice.proxies.entities;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.io.IOException;
import java.util.Optional;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext
public class EntitiesControllerTest extends AbstractControllerTest {

    @ClassRule
    public static final WireMockRule wireMockRule = new WireMockRule(9998);

    private MockMvc mockMvc;

    @Autowired
    private EntitiesService entitiesService;

    private EntitiesService spy;

    @BeforeClass
    public static void setUpOnce() {
        wireMockRule.stubFor(delete(urlPathEqualTo("/entities/12")).willReturn(aResponse().withStatus(200)));
        wireMockRule.stubFor(get(urlPathEqualTo("/entities"))
                .willReturn(aResponse().withStatus(200).withBody("").withFixedDelay(200)));
        wireMockRule.stubFor(put(urlPathEqualTo("/entities"))
                .willReturn(aResponse().withStatus(200).withBody("").withFixedDelay(200)));
    }

    @Before
    public void setUp() throws IOException {
        spy = Mockito.spy(entitiesService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(new EntitiesController(spy))
                .alwaysDo(MockMvcResultHandlers.print()).build();
    }

    @Test
    public void getEntities() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/entities?query=htg").header("Authorization", "Bearer 1"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(spy, VerificationModeFactory.times(1)).getEntities(Optional.of("1"), "htg", "");
    }

    @Test
    public void getEntitiesRest() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/entities?query=htg").header("Authorization", "Bearer 1"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(spy, VerificationModeFactory.times(1)).getEntities(Optional.of("1"), "htg", "");
    }

    @Test
    public void addEntities() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/entities").header("Authorization", "Bearer 1").content("true"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(spy, VerificationModeFactory.times(1)).addEntities(Optional.of("1"), "true");
    }

    @Test
    public void deleteEntities() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/entities/12").header("Authorization", "Bearer 1"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(spy, VerificationModeFactory.times(1)).deleteEntity(Optional.of("1"), "12");
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
