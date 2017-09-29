package de.zalando.zmon.dataservice.config.kairosdb;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class ReplicaIteratorTest {

    @Test
    public void testSimpleReplicasIteration() {
        final KairosDbStorageConfiguration cfg = new KairosDbStorageConfiguration();
        cfg.setReplicas(ImmutableList.of("foo", "bar"));
        final ReplicaIterator replicaIterator = cfg.replicaIterator();
        assertTrue(replicaIterator.hasNext());
        String replica = replicaIterator.next(0);
        assertThat(replica, is("foo"));
        replica = replicaIterator.next(0);
        assertThat(replica, is("bar"));
        assertFalse(replicaIterator.hasNext());
    }

    @Test
    public void testShardedReplication() {
        final KairosDbStorageConfiguration cfg = new KairosDbStorageConfiguration();
        final KairosDbReplicaConfiguration replica1 = new KairosDbReplicaConfiguration();
        replica1.setShards(ImmutableList.of("foo", "bar"));
        final KairosDbReplicaConfiguration replica2 = new KairosDbReplicaConfiguration();
        replica2.setShards(ImmutableList.of("a", "b", "c"));
        cfg.setShardedReplicas(ImmutableList.of(replica1, replica2));
        final ReplicaIterator replicaIterator = cfg.replicaIterator();
        assertTrue(replicaIterator.hasNext());
        String shardedReplica = replicaIterator.next(3);
        assertThat(shardedReplica, is("bar"));
        shardedReplica = replicaIterator.next(0);
        assertThat(shardedReplica, is("a"));
        assertFalse(replicaIterator.hasNext());
    }
}