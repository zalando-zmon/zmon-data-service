package de.zalando.zmon.dataservice.data;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

/**
 * Created by jmussler on 25.04.16.
 */
@Component
public class ProxyWriter {

    private final String forwardUrl;

    private Executor executor;

    private DataServiceMetrics metrics;

    @Autowired
    public ProxyWriter(DataServiceConfigProperties config, DataServiceMetrics metrics) {
        this.forwardUrl = config.getDataProxyUrl();
        this.metrics = metrics;

        if (null != forwardUrl) {
            executor = Executor.newInstance(getHttpClient(config.getDataProxySocketTimeout(), config.getDataProxyTimeout(), config.getDataProxyConnections()));
        }
        else {
            executor = null;
        }
    }

    public static HttpClient getHttpClient(int socketTimeout, int timeout, int maxConnections) {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(timeout).build();
        return HttpClients.custom().setMaxConnPerRoute(maxConnections).setMaxConnTotal(maxConnections).setDefaultRequestConfig(config).build();
    }

    /*
    * We will reuse the original request's token for the proxy call, that saves us some setup/dependency to token management
    * */
    @Async
    public void write(String token, String accountId, String checkId, String data) {
        if (null == forwardUrl && !"".equals(forwardUrl)) {
            return;
        }

        try {
            HttpResponse r = executor.execute(Request.Put(forwardUrl + "/api/v1/data/" + accountId + "/" + checkId + "/")
                                                    .useExpectContinue()
                                                    .addHeader("Authorization", "Bearer: " + token)
                                                    .bodyString(data, ContentType.APPLICATION_JSON)).returnResponse();

            if (r.getStatusLine().getStatusCode()!= 200) {
                metrics.markProxyError();
            }
        }
        catch (Exception ex) {
            metrics.markProxyError();
        }
    }
}
