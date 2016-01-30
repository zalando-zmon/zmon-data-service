package de.zalando.zmon.dataservice;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.zalando.riptide.Actions.pass;
import static org.zalando.riptide.Conditions.anyStatus;
import static org.zalando.riptide.Conditions.on;
import static org.zalando.riptide.Selectors.status;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.zalando.riptide.PassThroughResponseErrorHandler;
import org.zalando.riptide.Rest;
import org.zalando.riptide.ThrowingConsumer;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

@SpringApplicationConfiguration(classes = { Application.class })
@WebIntegrationTest(randomPort = true)
@ActiveProfiles("local-it")
public class DataServiceLocalIT extends AbstractControllerTest {

    private static final Logger log = LoggerFactory.getLogger(DataServiceLocalIT.class);

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
    public void startUp() throws InterruptedException, IOException {
        log.info("Service up and running, start with requests ...");
        TimeUnit.SECONDS.sleep(3);

        // try out RipTide
        RestTemplate rest = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        // seems not to work with SimpleClientHttpRequestFactory
        // RestTemplate rest = new RestTemplate();

        rest.setErrorHandler(new PassThroughResponseErrorHandler());
        Rest r = Rest.create(rest);
        r.execute(HttpMethod.GET, URI.create("http://localhost:" + port + "/api/v1/checks")).dispatch(status(),
                on(UNAUTHORIZED).call(pass()), anyStatus().call(new ThrowingConsumer<ClientHttpResponse, Exception>() {
                    @Override
                    public void accept(ClientHttpResponse input) throws AssertionError {
                        Assertions.fail("expect UNAUTHORIZED");
                    }
                }));

        // ResponseEntity<String> response1 = rest.exchange("http://localhost:"
        // + port + "/api/v1/checks", HttpMethod.GET, null, String.class);
        // Assertions.assertThat(response1).isNotNull();
        // Assertions.assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        rest.getInterceptors().add(new ClientHttpRequestInterceptor() {

            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
                    throws IOException {
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer: 987654321");
                return execution.execute(request, body);
            }
        });

        ResponseEntity<String> response2 = rest.exchange("http://localhost:" + port + "/api/v1/checks", HttpMethod.GET,
                null, String.class);
        Assertions.assertThat(response2).isNotNull();
        Assertions.assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

        // TRIAL_RUN
        String trialRunBody = super.resourceToString(jsonResource("data/trialRun"));
        RequestEntity<?> trailRunRequest = RequestEntity
                .put(URI.create("http://localhost:" + port + "/api/v1/data/trial-run/"))
                .contentType(MediaType.APPLICATION_JSON).body(trialRunBody);

        ResponseEntity<String> trialRunResponse = rest.exchange(trailRunRequest, String.class);
        Assertions.assertThat(trialRunResponse).isNotNull();
        Assertions.assertThat(trialRunResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // PUT_DATA
        String putData = super.resourceToString(jsonResource("data/trialRun"));
        RequestEntity<?> putDataRequest = RequestEntity
                .put(URI.create("http://localhost:" + port + "/api/v1/data/123456789/13/"))
                .contentType(MediaType.APPLICATION_JSON).body(putData);

        ResponseEntity<String> putDataResponse = rest.exchange(putDataRequest, String.class);
        Assertions.assertThat(putDataResponse).isNotNull();
        Assertions.assertThat(putDataResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        TimeUnit.SECONDS.sleep(40);
    }
}
