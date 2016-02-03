package de.zalando.zmon.dataservice.oauth2;

import org.springframework.security.oauth2.provider.OAuth2Authentication;

import com.google.common.cache.CacheLoader;

class AuthenticationCacheLoader extends CacheLoader<String, OAuth2Authentication> {

    private final CachingTokenInfoResourceServerTokenServices tokenService;

    public AuthenticationCacheLoader(CachingTokenInfoResourceServerTokenServices tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public OAuth2Authentication load(String key) throws Exception {
        return tokenService.doLoadAuthentication(key);
    }

}