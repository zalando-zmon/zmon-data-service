package de.zalando.zmon.dataservice.config;

import com.google.common.collect.ImmutableList;
import de.zalando.zmon.dataservice.config.kairosdb.KairosDbReplicaConfiguration;
import de.zalando.zmon.dataservice.config.kairosdb.KairosDbStorageConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration
@TestPropertySource(properties = "dataservice.storage.replicas = host1,host2")
public class DataServiceConfigPropertiesTest {

    @Configuration
    @EnableConfigurationProperties(DataServiceConfigProperties.class)
    static class Config {}

    @Autowired
    private DataServiceConfigProperties properties;

    @Test
    public void testNewConfiguration() {
        final List<String> replicas = properties.getStorage().getReplicas();
        assertThat(replicas, hasSize(2));
        assertThat(replicas, hasItem("host1"));
        assertThat(replicas, hasItem("host2"));
    }

    @Test
    public void testMigrationOfSimpleReplicas() {
        final DataServiceConfigProperties props = new DataServiceConfigProperties();
        props.setKairosdbWriteUrls(ImmutableList.of(ImmutableList.of("foo"), ImmutableList.of("bar")));
        props.migrateKairosDbStorageConfig();
        final KairosDbStorageConfiguration storageConfiguration = props.getStorage();
        final List<KairosDbReplicaConfiguration> shardedReplicas = storageConfiguration.getShardedReplicas();
        assertThat(shardedReplicas, empty());
        final List<String> replicas = storageConfiguration.getReplicas();
        assertThat(replicas, hasSize(2));
    }

    @Test
    public void testMigrationOfSingleReplica() {
        final DataServiceConfigProperties props = new DataServiceConfigProperties();
        props.setKairosdbWriteUrls(ImmutableList.of(ImmutableList.of("foo")));
        props.migrateKairosDbStorageConfig();
        final KairosDbStorageConfiguration storageConfiguration = props.getStorage();
        final List<KairosDbReplicaConfiguration> shardedReplicas = storageConfiguration.getShardedReplicas();
        assertThat(shardedReplicas, empty());
        final List<String> replicas = storageConfiguration.getReplicas();
        assertThat(replicas, hasSize(1));
    }

    @Test
    public void testMigrationOfSimpleShards() {
        final DataServiceConfigProperties props = new DataServiceConfigProperties();
        props.setKairosdbWriteUrls(ImmutableList.of(ImmutableList.of("foo", "bar")));
        props.migrateKairosDbStorageConfig();
        final KairosDbStorageConfiguration storageConfiguration = props.getStorage();
        final List<String> replicas = storageConfiguration.getReplicas();
        assertThat(replicas, empty());
        final List<KairosDbReplicaConfiguration> shardedReplicas = storageConfiguration.getShardedReplicas();
        assertThat(shardedReplicas, hasSize(1));
        final KairosDbReplicaConfiguration replicaConfiguration = shardedReplicas.get(0);
        assertThat(replicaConfiguration.getShards(), hasSize(2));
    }

    @Test
    public void testMigrationOfShardedReplicas() {
        final DataServiceConfigProperties props = new DataServiceConfigProperties();
        props.setKairosdbWriteUrls(ImmutableList.of(ImmutableList.of("foo", "bar"), ImmutableList.of("a", "b", "c")));
        props.migrateKairosDbStorageConfig();
        final KairosDbStorageConfiguration storageConfiguration = props.getStorage();
        final List<String> replicas = storageConfiguration.getReplicas();
        assertThat(replicas, empty());
        final List<KairosDbReplicaConfiguration> shardedReplicas = storageConfiguration.getShardedReplicas();
        assertThat(shardedReplicas, hasSize(2));
        KairosDbReplicaConfiguration replicaConfiguration = shardedReplicas.get(0);
        assertThat(replicaConfiguration.getShards(), hasSize(2));
        replicaConfiguration = shardedReplicas.get(1);
        assertThat(replicaConfiguration.getShards(), hasSize(3));
    }

    @Test
    public void testNewConfigurationPrecedence() {
        final DataServiceConfigProperties props = new DataServiceConfigProperties();
        props.getStorage().setReplicas(ImmutableList.of("a", "b", "c"));
        props.setKairosdbWriteUrls(ImmutableList.of(ImmutableList.of("foo", "bar"), ImmutableList.of("a", "b", "c")));
        props.migrateKairosDbStorageConfig();
        final KairosDbStorageConfiguration storageConfiguration = props.getStorage();
        final List<String> replicas = storageConfiguration.getReplicas();
        assertThat(replicas, hasSize(3));
        final List<KairosDbReplicaConfiguration> shardedReplicas = storageConfiguration.getShardedReplicas();
        assertThat(shardedReplicas, empty());
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingStorageConfigurationFails() {
        final DataServiceConfigProperties props = new DataServiceConfigProperties();
        props.migrateKairosDbStorageConfig();
    }
}