package de.zalando.zmon.dataservice.config.kairosdb;

import java.util.Iterator;
import java.util.List;

public class ReplicaIterator {
    private final KairosDbStorageConfiguration storageConfiguration;
    private int partitionCount = 1;
    private final Iterator replicaIterator;

    ReplicaIterator(final KairosDbStorageConfiguration storageConfiguration) {
        this.storageConfiguration = storageConfiguration;
        final List<String> replicas = this.storageConfiguration.getReplicas();
        if(replicas.isEmpty()) {
            final List<KairosDbReplicaConfiguration> shardedReplicas = this.storageConfiguration.getShardedReplicas();
            this.replicaIterator = shardedReplicas.iterator();
            this.partitionCount = shardedReplicas.size();
        } else {
            this.replicaIterator = replicas.iterator();
        }
    }

    public boolean hasNext() {
        return replicaIterator.hasNext();
    }

    public String next(final int key) {
        final Object next = replicaIterator.next();
        if(next instanceof KairosDbReplicaConfiguration) {
            final List<String> shards = ((KairosDbReplicaConfiguration) next).getShards();
            final int index = key % partitionCount;
            return shards.get(index);
        }
        return (String)next;
    }
}
