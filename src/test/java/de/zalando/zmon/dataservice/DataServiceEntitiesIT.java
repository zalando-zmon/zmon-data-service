package de.zalando.zmon.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.util.EntityUtils;
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
import java.net.URLEncoder;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.net.HttpURLConnection.HTTP_OK;

@SpringApplicationConfiguration(classes = {Application.class})
@WebIntegrationTest(randomPort = true)
@ActiveProfiles("entities")
public class DataServiceEntitiesIT extends AbstractControllerTest {

    private static final Logger log = LoggerFactory.getLogger(DataServiceEntitiesIT.class);
    private static final String BEARER_TOKEN = "Bearer 987654321";

    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${local.server.port}")
    private int port;

    @Rule
    public final WireMockRule wireMockRule = new WireMockRule(10082);

    @Rule
    public final WireMockRule wireMockRuleController = new WireMockRule(10081);


    @Before
    public void configureWireMockForCheck() throws IOException {
        wireMockRule.stubFor(get(urlPathEqualTo("/oauth2/tokeninfo"))
                .willReturn(WireMock.aResponse().withBody(resourceToString(jsonResource("tokeninfo")))
                        .withStatus(HTTP_OK).withHeader("Content-Type", "application/json")));


        wireMockRuleController.stubFor(get(urlPathEqualTo("/api/v1/entities/")).withQueryParam("query",  equalTo(URLEncoder.encode("[{\"id\":\"zmon-appliance-config\"}]", "UTF-8")))
                .willReturn(WireMock.aResponse().withBody(resourceToString(jsonResource("appliance-versions")))
                        .withStatus(HTTP_OK).withHeader("Content-Type", "application/json")));
    }


    @Test
    public void getConfigurationEntityTest() throws IOException {
        Request request = Request.Get("http://localhost:" + port + "/api/v1/appliance-versions");
        request.setHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN);

        Response r = Executor.newInstance().execute(request);
        HttpResponse rr = r.returnResponse();
        String body = EntityUtils.toString(rr.getEntity());

        Assertions.assertThat(rr.getStatusLine().getStatusCode()).isEqualTo(200);

        JsonNode node = mapper.readTree(body);
        Assertions.assertThat(node.has("redis")).isTrue();
    }
}
