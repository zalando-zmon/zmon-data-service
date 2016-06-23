package de.zalando.zmon.dataservice.oauth2;

import org.apache.http.client.fluent.Request;

import java.util.Optional;

public class BearerToken {
    public static Optional<String> extractFromHeader(String header) {
        try {
            return Optional.ofNullable(header.substring(7)); // Bearer
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    public static Request inject(Request request, Optional<String> token) {
        if (token.isPresent()) {
            return request.addHeader("Authorization", "Bearer " + token);
        } else {
            return request;
        }

    }
}
