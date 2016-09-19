package de.zalando.zmon.dataservice.proxies;

import de.zalando.zmon.dataservice.data.KairosDBStore;
import org.assertj.core.api.Assertions;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jmussler on 19.09.16.
 */
public class KairosDBTest {

    @Test
    public void testReplace() {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> entity = new HashMap<>();
        entity.put("id", "host01");
        entity.put("stack_name", "data-service");
        entity.put("application_id", "zmon-data-service");

        Map<String, String> tags = KairosDBStore.getTags("fragment.fjo:rd/c@rt?hide_footer=true", "host01", entity);

        Map<String, String> expected = new HashMap<>();
        expected.put("key", "fragment.fjo_rd/c_rt_hide_footer_true");
        expected.put("stack_name", "data-service");
        expected.put("application_id", "zmon-data-service");
        expected.put("entity", "host01");
        expected.put("metric", "fjo_rd/c_rt_hide_footer_true");

        Assertions.assertThat(tags).isEqualTo(expected);
    }
}
