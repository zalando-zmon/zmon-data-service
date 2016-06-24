package de.zalando.zmon.dataservice.proxies.checks;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import de.zalando.zmon.dataservice.proxies.ControllerProxy;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.apache.http.impl.client.HttpClients;

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

}
