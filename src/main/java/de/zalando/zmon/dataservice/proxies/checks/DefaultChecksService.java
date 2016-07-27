package de.zalando.zmon.dataservice.proxies.checks;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import de.zalando.zmon.dataservice.proxies.ControllerProxy;
import org.apache.http.client.fluent.Request;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

public class DefaultChecksService extends ControllerProxy implements ChecksService {

    public DefaultChecksService(DataServiceConfigProperties config)
    {
        super(config);
    }

    @Override
    public String allActiveAlertDefinitions(Optional<String> token, String query) throws URISyntaxException, IOException {
        URI uri = uri("/checks/all-active-alert-definitions").setParameter("query", query).build();
        return proxy(Request.Get(uri), token);
    }

    @Override
    public String allActiveCheckDefinitions(Optional<String> token, String query) throws URISyntaxException, IOException {
        URI uri = uri("/checks/all-active-check-definitions").setParameter("query", query).build();
        return proxy(Request.Get(uri), token);
    }

    @Override
    public String allActiveAlertDefinitionsLastModified(Optional<String> token, String query) throws URISyntaxException, IOException {
        URI uri = uri("/checks/all-active-alert-definitions").setParameter("query", query).build();
        return proxy(Request.Head(uri), token);
    }

    @Override
    public String allActiveCheckDefinitionsLastModified(Optional<String> token, String query) throws URISyntaxException, IOException {
        URI uri = uri("/checks/all-active-check-definitions").setParameter("query", query).build();
        return proxy(Request.Head(uri), token);
    }

}
