package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RedisDataStoreTest {

    @Autowired
    private DataServiceMetrics metrics;

    @Test
    public void testSerializeValue() {
        ObjectMapper mapper = new ObjectMapper();
        RedisDataStore ds = new RedisDataStore(null, mapper, null, metrics);
        CheckData cd = new CheckData();
        ObjectNode node = mapper.createObjectNode();
        node.put("value", "foobar");
        cd.check_result = node;
        String value = ds.buildValue(new AlertData(), cd);
        Assertions.assertThat(value).contains("\"value\":\"foobar\"");
    }
}
