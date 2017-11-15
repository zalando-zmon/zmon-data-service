package de.zalando.zmon.dataservice.data;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ThreadLocalRandom;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by mabdelhameed on 15/11/2017.
 */
public class KairosDataPointsQueryStore implements DataPointsQueryStore {

    private static final Logger LOG = LoggerFactory.getLogger(KairosDBStore.class);

    private final Executor executor;
    private final DataServiceConfigProperties config;

    @Autowired
    KairosDataPointsQueryStore(DataServiceConfigProperties config) {
        this.config = config;

        LOG.info("KairosDB settings connections={} socketTimeout={} timeout={}", config.getKairosdbConnections(), config.getKairosdbSockettimeout(), config.getKairosdbTimeout());
        executor = Executor.newInstance(getHttpClient(config.getKairosdbSockettimeout(), config.getKairosdbTimeout(), config.getKairosdbConnections()));
    }

    public static HttpClient getHttpClient(int socketTimeout, int timeout, int maxConnections) {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(timeout).build();
        return HttpClients.custom().setMaxConnPerRoute(maxConnections).setMaxConnTotal(maxConnections).setDefaultRequestConfig(config).build();
    }

    public int store(String query) {
        int error_count = 0;
        for (List<String> urls : config.getKairosdbWriteUrls()) {
            final int index = ThreadLocalRandom.current().nextInt(urls.size());
            final String url = urls.get(index);

            try {
                executor.execute(Request.Post(url + "/api/v1/datapoints").bodyString(query, ContentType.APPLICATION_JSON)).returnContent().asString();
            } catch (IOException ex) {
                if (config.isLogKairosdbErrors()) {
                    LOG.error("KairosDB write failed url={}", url, ex);
                }
                error_count+=1;
            }
        }

        return error_count;
    }
}
