package de.zalando.zmon.dataservice.data;

import com.google.common.collect.ImmutableList;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import de.zalando.zmon.dataservice.config.kairosdb.KairosDbStorageConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestingProperties {
    @Bean
    public DataServiceConfigProperties dataServiceConfigProperties() {
        final DataServiceConfigProperties properties = new DataServiceConfigProperties();
        final KairosDbStorageConfiguration storage = new KairosDbStorageConfiguration();
        storage.setReplicas(ImmutableList.of("foo"));
        properties.setStorage(storage);
        return properties;
    }
}
