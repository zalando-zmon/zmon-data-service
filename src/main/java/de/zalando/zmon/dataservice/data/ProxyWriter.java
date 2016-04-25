package de.zalando.zmon.dataservice.data;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.springframework.stereotype.Component;

/**
 * Created by jmussler on 25.04.16.
 */
@Component
public class ProxyWriter {

    private final String forwardUrl;

    public ProxyWriter(DataServiceConfigProperties configuration) {
        forwardUrl = configuration.getDataProxyUrl();
    }

    public void write(String accountId, String checkId, String data) {

    }
}
