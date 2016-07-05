package de.zalando.zmon.dataservice;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.net.HttpURLConnection.HTTP_OK;

@SpringApplicationConfiguration(classes = {Application.class})
@WebIntegrationTest(randomPort = true)
@ActiveProfiles("it")
public class DataServiceIT extends AbstractControllerTest {

    private static final Logger log = LoggerFactory.getLogger(DataServiceIT.class);
    private static final String BEARER_TOKEN = "Bearer 987654321";

    @Value("${local.server.port}")
    private int port;

    @Rule
    public final WireMockRule wireMockRule = new WireMockRule(10080);

    @Before
    public void configureWireMockForCheck() throws IOException {
        wireMockRule.stubFor(get(urlPathEqualTo("/oauth2/tokeninfo"))
                .willReturn(WireMock.aResponse().withBody(resourceToString(jsonResource("tokeninfo")))
                        .withStatus(HTTP_OK).withHeader("Content-Type", "application/json")));
    }

    @Test
    public void getCheckDefinitions() throws IOException {
        Request request = Request.Get("http://localhost:" + port + "/api/v1/checks/all-active-check-definitions");
        try {
            Executor.newInstance().execute(request).returnContent().toString();
            Assertions.fail("expect UNAUTHORIZED");
        } catch (HttpResponseException e) {
            Assertions.assertThat(e.getStatusCode()).isEqualTo(401);
        }

        Request request2 = Request.Get("http://localhost:" + port + "/api/v1/checks/all-active-check-definitions");
        request2.setHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN);
        Executor.newInstance().execute(request2).returnContent().toString();
    }

    @Test
    public void getAlertDefinitions() throws IOException {
        Request request = Request.Get("http://localhost:" + port + "/api/v1/checks/all-active-alert-definitions");
        request.setHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN);
        Executor.newInstance().execute(request).returnContent().toString();
    }

    @Test
    public void putData() throws IOException {
        Request request = Request.Put("http://localhost:" + port + "/api/v1/data/dc:123/456/").bodyString("{}", ContentType.APPLICATION_JSON);
        request.setHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN);
        Executor.newInstance().execute(request).returnContent().toString();
    }

    @Test
    public void putTrialRun() throws IOException {
        String trialRunBody = super.resourceToString(jsonResource("data/trialRun"));
        Request request = Request.Put("http://localhost:" + port + "/api/v1/data/trial-run/").bodyString(trialRunBody, ContentType.APPLICATION_JSON);
        request.setHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN);
        Executor.newInstance().execute(request).returnContent().toString();
    }

    @Test
    public void getEntities() throws IOException {
        Request request = Request.Get("http://localhost:" + port + "/api/v1/entities");
        request.setHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN);
        Executor.newInstance().execute(request).returnContent().toString();
    }

    @Test
    public void getConfigurationEntityFails() throws IOException {
        Request request = Request.Get("http://localhost:" + port + "/api/v1/appliance-versions");
        request.setHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN);
        try {
            Executor.newInstance().execute(request).returnContent().toString();
            Assertions.fail("Request should fail");
        } catch (HttpResponseException e) {
            Assertions.assertThat(e.getStatusCode()).isEqualTo(500);
        }
    }
}
