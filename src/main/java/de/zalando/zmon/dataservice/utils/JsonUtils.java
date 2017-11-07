package de.zalando.zmon.dataservice.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import org.springframework.util.NumberUtils;

import java.math.BigDecimal;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;


/**
 * @author abeverage
 */
public class JsonUtils {

    private static class CheckField {
        final String prefixedName;
        final JsonNode jsonNode;

        public CheckField(String prefixedName, JsonNode jsonNode) {
            this.prefixedName = prefixedName;
            this.jsonNode = jsonNode;
        }
    }

    /**
     * Filters non-NumericNode, or TextNodes containing numeric values, from a flat map
     * of JsonNodes.
     *
     * @param flatMap
     * @return
     */
    public static Map<String, NumericNode> flatMapJsonNumericNodes(Map<String, JsonNode> flatMap) {

        checkNotNull(flatMap);

        Map<String, NumericNode> metricsMap = newHashMap();

        for (String key : flatMap.keySet()) {
            if (flatMap.get(key) instanceof NumericNode) {
                metricsMap.put(key, (NumericNode) flatMap.get(key));
            } else if (flatMap.get(key) instanceof TextNode) {
                try {
                    TextNode t = (TextNode) flatMap.get(key);
                    BigDecimal db = new BigDecimal(t.textValue());
                    DecimalNode dn = new DecimalNode(db);
                    metricsMap.put(key, dn);
                } catch (NumberFormatException ex) {
                    // Ignore
                }
            }
        }

        return metricsMap;
    }


    /**
     * Flat maps all value types in a JSON blob from a given JsonNode.
     *
     * @param root
     * @return
     */
    public static Map<String, JsonNode> flatMapJsonNode(JsonNode root) {

        checkNotNull(root);

        Map<String, JsonNode> result = newHashMap();
        Deque<CheckField> bases = new ArrayDeque<>();
        bases.push(new CheckField("", root));

        while (!bases.isEmpty()) {
            CheckField current = bases.pop();

            if (ValueNode.class.isAssignableFrom(current.jsonNode.getClass())) {
                result.put(current.prefixedName, current.jsonNode);
            }

            Iterator<String> childFields = current.jsonNode.fieldNames();
            while (childFields.hasNext()) {
                String fieldName = childFields.next();

                if (current.prefixedName.length() == 0) {
                    bases.push(new CheckField(fieldName, current.jsonNode.get(fieldName)));
                } else {
                    bases.push(new CheckField(current.prefixedName + "." + fieldName, current.jsonNode.get(fieldName)));
                }
            }
        }

        return result;
    }
}
