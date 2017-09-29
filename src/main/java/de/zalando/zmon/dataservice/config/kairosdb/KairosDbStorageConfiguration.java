package de.zalando.zmon.dataservice.config.kairosdb;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class KairosDbStorageConfiguration {
    private List<KairosDbReplicaConfiguration> shardedReplicas = ImmutableList.of();

    private List<String> replicas = ImmutableList.of();

    public List<KairosDbReplicaConfiguration> getShardedReplicas() {
        return shardedReplicas;
    }

    public void setShardedReplicas(final List<KairosDbReplicaConfiguration> shardedReplicas) {
        this.shardedReplicas = shardedReplicas;
    }

    public List<String> getReplicas() {
        return replicas;
    }

    public void setReplicas(final List<String> replicas) {
        this.replicas = replicas;
    }

    public ReplicaIterator replicaIterator() {
        return new ReplicaIterator(this);
    }

}
