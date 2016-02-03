package de.zalando.zmon.dataservice.oauth2;

import org.springframework.security.core.AuthenticationException;

class CacheReadAuthenticationException extends AuthenticationException {
    private static final long serialVersionUID = 1L;

    public CacheReadAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}