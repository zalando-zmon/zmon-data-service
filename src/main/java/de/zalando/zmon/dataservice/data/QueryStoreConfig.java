package de.zalando.zmon.dataservice.data;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

/**
 * Created by mabdelhameed on 14/11/2017.
 */
@Configuration
public class QueryStoreConfig {

    @Bean
    DataPointsQueryStore dataPointsQueryStore(DataServiceConfigProperties config) {
        if (config.isDatapointsRedisEnabled()) {
            return new RedisDataPointsQueryStore(config);
        }

        return new KairosDataPointsQueryStore(config);
    }

}
