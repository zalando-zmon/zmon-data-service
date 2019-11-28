package de.zalando.zmon.dataservice.proxies.checks;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import de.zalando.zmon.dataservice.proxies.ControllerProxy;
import org.apache.http.client.fluent.Request;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class DefaultChecksService extends ControllerProxy implements ChecksService {

    DefaultChecksService(DataServiceConfigProperties config, CircuitBreakerFactory cbFactory) {
        super(cbFactory, config);
    }

    @Override
    public String allActiveAlertDefinitions(Optional<String> token, String query) throws URISyntaxException {
        URI uri = uri("/checks/all-active-alert-definitions").setParameter("query", query).build();
        return proxy(Request.Get(uri), token);
    }

    @Override
    public String allActiveCheckDefinitions(Optional<String> token, String query) throws URISyntaxException {
        URI uri = uri("/checks/all-active-check-definitions").setParameter("query", query).build();
        return proxy(Request.Get(uri), token);
    }

    @Override
    public String allActiveAlertDefinitionsLastModified(Optional<String> token, String query) throws URISyntaxException {
        URI uri = uri("/checks/all-active-alert-definitions").setParameter("query", query).build();
        return proxyForLastModifiedHeader(Request.Head(uri), token);
    }

    @Override
    public String allActiveCheckDefinitionsLastModified(Optional<String> token, String query) throws URISyntaxException {
        URI uri = uri("/checks/all-active-check-definitions").setParameter("query", query).build();
        return proxyForLastModifiedHeader(Request.Head(uri), token);
    }

}
