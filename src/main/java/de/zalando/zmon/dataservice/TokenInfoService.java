package de.zalando.zmon.dataservice;

import java.util.Optional;

public interface TokenInfoService {
    Optional<String> lookupUid(String authorizationHeaderValue);
}
