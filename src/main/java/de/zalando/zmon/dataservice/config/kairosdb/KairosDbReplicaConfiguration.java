package de.zalando.zmon.dataservice.config.kairosdb;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class KairosDbReplicaConfiguration {
    private List<String> shards = ImmutableList.of();

    public List<String> getShards() {
        return shards;
    }

    public void setShards(final List<String> shards) {
        this.shards = shards;
    }
}
