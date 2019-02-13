package de.zalando.zmon.dataservice.data;

import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.apache.http.client.fluent.Executor;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.apache.http.client.fluent.Request.Post;

public class M3DbDataPointsQueryStore implements DataPointsQueryStore {

  private static final Logger LOG = LoggerFactory.getLogger(M3DbDataPointsQueryStore.class);
  private final DataServiceConfigProperties config;
  private final Executor executor;
  private static final String M3DB_METRIC_WRITE_END_POINT = "/writetagged";

  @Autowired
  public M3DbDataPointsQueryStore(DataServiceConfigProperties config) {
    this.config = config;
    executor =
        HttpClientFactory.getExecutor(
            config.getM3dbSocketTimeout(),
            config.getM3dbTimeout(),
            config.getM3dbConnections(),
            config.getConnectionsTimeToLive());
  }

  public int store(String query) {
    int errorCount = 0;
    for (List<String> urls : config.getM3DbWriteUrls()) {
      final int index = ThreadLocalRandom.current().nextInt(urls.size());
      final String url = urls.get(index) + M3DB_METRIC_WRITE_END_POINT;
      try {
        executor
            .execute(Post(url).bodyString(query, ContentType.APPLICATION_JSON))
            .discardContent();
      } catch (IOException ex) {
        if (config.isLogM3dbErrors()) {
          LOG.error("M3DB write failed url={}", url, ex);
        }
        errorCount += 1;
      }
    }
    return errorCount;
  }
}
