package de.zalando.zmon.dataservice.proxies;

import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import de.zalando.zmon.dataservice.data.DataPointsQueryStore;
import de.zalando.zmon.dataservice.data.KairosDBStore;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * Created by jmussler on 19.09.16.
 */
public class KairosDBTest {

    @Test
    public void testReplace() {
        KairosDBStore store = new KairosDBStore(mock(DataServiceConfigProperties.class), mock(DataServiceMetrics.class),
                mock(DataPointsQueryStore.class));

        Map<String, String> entity = new HashMap<>();
        entity.put("id", "host01[aws:1234]");
        entity.put("stack_name", "data-service");
        entity.put("application_id", "zmon-data-service");

        Map<String, String> tags = store.getTags("fragment.fjo:rd/c@rt?hide_footer=true", "host01[aws:1234]", entity);

        Map<String, String> expected = new HashMap<>();
        expected.put("key", "fragment.fjo_rd/c_rt_hide_footer_true");
        expected.put("stack_name", "data-service");
        expected.put("application_id", "zmon-data-service");
        expected.put("entity", "host01_aws_1234_");
        expected.put("metric", "fjo_rd/c_rt_hide_footer_true");

        Assertions.assertThat(tags).isEqualTo(expected);
    }
}
