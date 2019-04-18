package de.zalando.zmon.dataservice.data;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

import org.apache.http.client.fluent.Executor;
import org.apache.http.entity.ContentType;

import java.util.concurrent.ThreadLocalRandom;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.http.client.fluent.Request.Post;


/**
 * Created by mabdelhameed on 15/11/2017.
 */
public class KairosDataPointsQueryStore implements DataPointsQueryStore {

    private static final Logger LOG = LoggerFactory.getLogger(KairosDataPointsQueryStore.class);

    private final Executor executor;
    private final DataServiceConfigProperties config;

    KairosDataPointsQueryStore(DataServiceConfigProperties config) {
        this.config = config;

        LOG.info("KairosDB settings connections={} socketTimeout={} timeout={}", config.getKairosdbConnections(),
                config.getKairosdbSocketTimeout(), config.getKairosdbTimeout());
        executor = HttpClientFactory.getExecutor(
                config.getKairosdbSocketTimeout(),
                config.getKairosdbTimeout(),
                config.getKairosdbConnections(),
                config.getConnectionsTimeToLive()
        );
    }

    public int store(String query) {
        int error_count = 0;
        for (List<String> urls : config.getKairosdbWriteUrls()) {
            final int index = ThreadLocalRandom.current().nextInt(urls.size());
            final String url = urls.get(index) + "/api/v1/datapoints";

            try {
                executor.execute(Post(url).bodyString(query, ContentType.APPLICATION_JSON)).discardContent();
            } catch (IOException ex) {
                if (config.isLogKairosdbErrors()) {
                    LOG.error("KairosDB write failed url={}", url, ex);
                }
                error_count += 1;
            }
        }

        return error_count;
    }
}
