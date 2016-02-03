package de.zalando.zmon.dataservice.oauth2;

import java.util.concurrent.ExecutionException;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception used when a {@link ExecutionException} was thrown during cache
 * read.
 * 
 * @author jbellmann
 *
 */
class CacheReadAuthenticationException extends AuthenticationException {
    private static final long serialVersionUID = 1L;

    public CacheReadAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}