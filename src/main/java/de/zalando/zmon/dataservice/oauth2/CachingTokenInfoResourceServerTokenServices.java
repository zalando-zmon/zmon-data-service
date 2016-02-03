package de.zalando.zmon.dataservice.oauth2;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.zalando.stups.oauth2.spring.server.AuthenticationExtractor;
import org.zalando.stups.oauth2.spring.server.TokenInfoResourceServerTokenServices;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

/**
 * 
 * @author jbellmann
 *
 */
public class CachingTokenInfoResourceServerTokenServices extends TokenInfoResourceServerTokenServices {

    private final Logger log = LoggerFactory.getLogger(CachingTokenInfoResourceServerTokenServices.class);

    private final LoadingCache<String, OAuth2Authentication> cache;

    public CachingTokenInfoResourceServerTokenServices(String tokenInfoEndpointUrl, String clientId,
            AuthenticationExtractor authenticationExtractor, RestTemplate restTemplate) {

        this(tokenInfoEndpointUrl, clientId, authenticationExtractor, restTemplate, getDefaultCacheBuilder());
    }

    public CachingTokenInfoResourceServerTokenServices(String tokenInfoEndpointUrl, String clientId,
            AuthenticationExtractor authenticationExtractor, RestTemplate restTemplate,
            CacheBuilder<String, OAuth2Authentication> cacheBuilder) {
        super(tokenInfoEndpointUrl, clientId, authenticationExtractor, restTemplate);

        cache = cacheBuilder.build(new AuthenticationCacheLoader(this));
    }

    /**
     * {@link CacheBuilder} with max-entries = 1000 and expireAfterWrite = 4
     * HOURS.
     * 
     * @return
     */
    public static CacheBuilder<String, OAuth2Authentication> getDefaultCacheBuilder() {
        return getCacheBuilder(1000, 4);
    }

    /**
     * {@link CacheBuilder} factory method.
     * 
     * @param maxSize
     * @param expireAfter
     *            in HOURS
     * @return
     */
    public static CacheBuilder<String, OAuth2Authentication> getCacheBuilder(int maxSize, int expireAfter) {
        return CacheBuilder.newBuilder().maximumSize(maxSize).expireAfterWrite(expireAfter, TimeUnit.HOURS)
                .removalListener(new CacheRemovalListener());
    }

    @Override
    public OAuth2Authentication loadAuthentication(String accessToken)
            throws AuthenticationException, InvalidTokenException {

        Assert.hasText(accessToken, "'accessToken' should never be null or empty");

        OAuth2Authentication authentication;
        try {
            authentication = cache.get(accessToken);
            return authentication;
        } catch (ExecutionException e) {
            log.error("Error getting Authentication from cache : {}", accessToken.substring(0, 6));
            throw new CacheReadAuthenticationException(e.getMessage(), e);
        }
    }

    /**
     * Delegates to super for cacheLoading.
     * 
     * @param accessToken
     * @return
     */
    OAuth2Authentication doLoadAuthentication(String accessToken) {
        return super.loadAuthentication(accessToken);
    }
}
