package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class M3DbStore {
    private static final Logger LOG = LoggerFactory.getLogger(KairosDBStore.class);
    private final DataServiceConfigProperties config;


    private final DataPointsQueryStore dataPointsQueryStore;
    private final DataServiceMetrics metrics;


    @Autowired
    M3DbStore(DataPointsQueryStore dataPointsQueryStore, DataServiceMetrics metrics, DataServiceConfigProperties config) {
        this.dataPointsQueryStore = dataPointsQueryStore;
        this.metrics = metrics;
        this.config = config;
    }

    void store(WorkerResult wr) {
        if (!config.isM3DbEnabled()) {
            return;
        }

        if (wr == null || wr.results == null || wr.results.isEmpty()) {
            LOG.warn("Received a request with invalid results: {}", wr);
            return;
        }

        try {
            String query = "";

            for (CheckData checkData : wr.results) {

                // TODO: Move Sampling login to the generic data model

                Map<String, NumericNode> value = new HashMap<>();
                String timeSeries_id = "zmon.check.id." + checkData.checkId;

                fillFlatValueMap(value, "", checkData.checkResult.get("value"));

                //TODO: Translate ZMON metrics format to M3DB metrics format
            }

            int err = dataPointsQueryStore.store(query);
            if (err > 0) {
                metrics.markM3DbError();
            }
        } catch (Exception e) {
            LOG.error("M3Db write failed", e);
        }
    }

    /**
     * TODO: This utility function logic needs to move to generic data model. This is not specific to M3DB
     * Flattens the JSONNode that contains a single check results into a map
     *
     * @param values
     * @param prefix
     * @param base
     */
    public void fillFlatValueMap(Map<String, NumericNode> values, String prefix, JsonNode base) {
        if (base instanceof NumericNode) {
            values.put(prefix, (NumericNode) base);
        } else if (base instanceof TextNode) {
            // try to convert string node in case it is numeric
            try {
                TextNode t = (TextNode) base;
                BigDecimal db = new BigDecimal(t.textValue());
                DecimalNode dn = new DecimalNode(db);
                values.put(prefix, dn);
            } catch (NumberFormatException ex) {
                // Ignore
            }
        } else if (base instanceof ObjectNode) {
            Iterator<String> i = base.fieldNames();
            while (i.hasNext()) {
                String k = i.next();

                if (prefix.length() == 0) {
                    fillFlatValueMap(values, k, base.get(k));
                } else {
                    fillFlatValueMap(values, prefix + "." + k, base.get(k));
                }
            }
        }
    }
}
