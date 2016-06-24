package de.zalando.zmon.dataservice.oauth2;

import org.apache.http.client.fluent.Request;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class BearerToken {
    public static Optional<String> extractFromHeader(String header) {
        try {
            return Optional.ofNullable(header.substring(7)); // Bearer
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    public static Optional<String> extract(HttpServletRequest request) {
        // really stupid, but somehow the "Authorization" header gets lost in Spring magic for GET requests..
        Object value = request.getAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_VALUE);
        if (value == null) {
            return extractFromHeader(request.getHeader(AUTHORIZATION));
        } else {
            return Optional.of(String.valueOf(value));
        }
    }

    public static Request inject(Request request, Optional<String> token) {
        if (token.isPresent()) {
            return request.addHeader(AUTHORIZATION, "Bearer " + token.get());
        } else {
            return request;
        }
    }
}
