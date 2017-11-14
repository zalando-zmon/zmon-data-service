package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import de.zalando.zmon.dataservice.utils.JsonUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by abeverage on 11/1/17.
 */
@RunWith(DataProviderRunner.class)
public class JsonFlatMapTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    @UseDataProvider("maps")
    public void emptyMap(String input, String expectedResult, int totalNodesCount, int numericNodesCount) throws IOException {
        JsonNode inputRoot = mapper.readTree(input);

        Map<String, JsonNode> allFieldsMap = JsonUtils.flatMapJsonNode(inputRoot);
        Map<String, NumericNode> numericFieldsMap = JsonUtils.flatMapJsonNumericNodes(allFieldsMap);

        //  Some convolutedness required for validation if we keep the Kairos writer
        //  working with Map<String, NumericNode> instead of a json tree:
        JsonNode actualTree = mapper.readTree(mapper.writeValueAsString(numericFieldsMap));
        JsonNode expectedTree = mapper.readTree(expectedResult);

        assertThat(actualTree).isEqualTo(expectedTree);
        assertThat(allFieldsMap.size()).isEqualTo(totalNodesCount);
        assertThat(numericFieldsMap.size()).isEqualTo(numericNodesCount);
    }

    @DataProvider
    public static Object[][] maps() {
        return new Object[][] {
                { "{\"foo\": \"bar\"}", "{}", 1, 0 },
                { "{\"foo\": 1}", "{\"foo\":1}", 1, 1 },
                { "{\"foo\": \"1\"}", "{\"foo\":1}", 1, 1 },
                { "{\"foo\":{\"bar1\":1,\"bar2\":\"two\",\"bar3\":3.4}}", "{\"foo.bar1\":1,\"foo.bar3\":3.4}", 3, 2 },
                { "{\"foo\":{\"bar1\":1,\"bar2\":\"two\",\"bar3\":\"3.4\"}}", "{\"foo.bar1\":1,\"foo.bar3\":3.4}", 3, 2 },
                { "{\"foo\":{\"bar1\":1,\"bar2\":\"two\",\"bar3\":{\"baz1\":\"asdf\",\"baz2\":{\"zomg\":31415}}}}",
                    "{\"foo.bar1\":1,\"foo.bar3.baz2.zomg\":31415}", 4, 2 },
                { "{\"foo.bar1\":2,\"foo\":{\"bar1\":1,\"bar2\":\"two\",\"bar3\":{\"baz1\":\"asdf\",\"baz2\":{\"zomg\":31415}}}}",
                        "{\"foo.bar1\":2,\"foo.bar3.baz2.zomg\":31415}", 4, 2 }
                        // ^-- Name collisions are not detectable keeping the implementation as-was.
        };
    }
}
