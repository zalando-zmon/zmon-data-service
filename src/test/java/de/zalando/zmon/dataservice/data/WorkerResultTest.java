package de.zalando.zmon.dataservice.data;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class WorkerResultTest {
    @Test
    public void testToString() {
        WorkerResult wr = new WorkerResult();
        assertTrue(wr.toString().contains("no results"));
        wr.results = null;
        assertTrue(wr.toString().contains("no results"));
        wr.results = ImmutableList.of(new CheckData());
        assertTrue(wr.toString().contains("1 result"));
        assertTrue(wr.toString().contains("account="));
        assertTrue(wr.toString().contains("team="));
    }

}