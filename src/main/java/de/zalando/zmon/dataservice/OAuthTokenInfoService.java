package de.zalando.zmon.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by jmussler on 8/12/15.
 */
public class OAuthTokenInfoService implements TokenInfoService {
    private static final Logger LOG = LoggerFactory.getLogger(OAuthTokenInfoService.class);

    private final String serviceUrl;
    private final Executor executor;
    private final ObjectMapper mapper = new ObjectMapper();

    // oauthtoken -> uid
    private final Cache<String, String> tokenCache = CacheBuilder.newBuilder().expireAfterWrite(600, TimeUnit.SECONDS).build();

    public OAuthTokenInfoService(String serviceUrl) {
        LOG.info("Setting token service url to {}", serviceUrl);
        this.serviceUrl = serviceUrl;
        this.executor = Executor.newInstance();
    }

    @Override
    public Optional<String> lookupUid(String authorizationHeaderValue) {
        if (Strings.isNullOrEmpty(authorizationHeaderValue)) {
            return Optional.empty();
        }

        return queryCacheOrServer(authorizationHeaderValue.replace("Bearer ", ""));
    }

    private Optional<String> queryCacheOrServer(String oauthToken) {
        try {
            return Optional.ofNullable(
                    tokenCache.get(oauthToken, () -> queryOAuthServer(oauthToken))
            );
        } catch (Exception e) {
            LOG.error("Error querying oauth server for token: " + oauthToken, e);
            return Optional.empty();
        }
    }

    private String queryOAuthServer(String token) throws Exception {
        // throws exception in != 200 status code
        String body = executor.execute(Request.Get(serviceUrl + token)).returnContent().asString();
        JsonNode result = mapper.readTree(body);
        if (result.has("uid") && result.get("expires_in").asInt() > 0) {
            return result.get("uid").asText();
        }
        return null;
    }
}
