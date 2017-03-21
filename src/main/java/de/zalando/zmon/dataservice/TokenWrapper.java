package de.zalando.zmon.dataservice;

import org.zalando.stups.tokens.AccessTokens;

/**
 * Created by jmussler on 21.03.17.
 */
public class TokenWrapper {

    private final String token;
    private final AccessTokens tokens;
    private final String tokenId;

    public TokenWrapper(AccessTokens tokens, String tokenId) {
        this.tokenId = tokenId;
        this.tokens = tokens;
        token = null;
    }

    public TokenWrapper(String token) {
        this.tokenId = null;
        this.tokens = null;
        this.token = token;
    }

    public String get() {
        if (tokens != null) {
            return tokens.get(tokenId);
        }
        return token;
    }
}

