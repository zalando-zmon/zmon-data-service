package de.zalando.zmon.dataservice.data;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;

/**
 * Created by jmussler on 5/8/15.
 */
@Service
public class KairosDBStore {

    private static final Logger LOG = LoggerFactory.getLogger(KairosDBStore.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final DataServiceConfigProperties config;

    private final static Set<String> TAG_FIELDS = new HashSet<>(Arrays.asList("application_id","application_version","stack_name","stack_version"));

    public void fillFlatValueMap(Map<String, NumericNode> values, String prefix, JsonNode base) {
        if(base instanceof NumericNode) {
            values.put(prefix, (NumericNode)base);
        }
        else if (base instanceof TextNode) {
            // try to convert string node in case it is numeric
            try {
                TextNode t = (TextNode)base;
                BigDecimal db = new BigDecimal(t.textValue());
                DecimalNode dn = new DecimalNode(db);
                values.put(prefix, dn);
            }
            catch(NumberFormatException ex) {
                // Ignore
            }
        }
        else if  (base instanceof ObjectNode) {
            Iterator<String> i = base.fieldNames();
            while(i.hasNext()) {
                String k = i.next();

                if(prefix.length()==0) {
                    fillFlatValueMap(values, k, base.get(k));
                }
                else {
                    fillFlatValueMap(values, prefix+"."+k, base.get(k));
                }
            }
        }
    }

    private final String url;
    private final DataServiceMetrics metrics;
    private final Executor executor;

    private static class DataPoint {
        public String name;
        public List<ArrayNode> datapoints = new ArrayList<>(1);
        public Map<String, String> tags = new HashMap<>();
    }

    public static HttpClient getHttpClient(int socketTimeout, int timeout, int maxConnections) {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(timeout).build();
        return HttpClients.custom().setMaxConnPerRoute(maxConnections).setMaxConnTotal(maxConnections).setDefaultRequestConfig(config).build();
    }

    @Autowired
    public KairosDBStore(DataServiceConfigProperties config, DataServiceMetrics metrics) {
        this.metrics = metrics;
        this.config = config;
        this.url = "http://"+config.getKairosdbHost() +":"+config.getKairosdbPort() +"/api/v1/datapoints";

        executor = Executor.newInstance(getHttpClient(config.getKairosDBSockeTimeout(), config.getKairosDBTimeout(), config.getKairosDBConnections()));
    }

    public static String extractMetricName(String key) {
        if(null==key || "".equals(key)) return null;
        String[] keyParts = key.split("\\.");
        String metricName = keyParts[keyParts.length-1];
        if("".equals(metricName)) {
            metricName = keyParts[keyParts.length-2];
        }
        return metricName;
    }

    public void store(WorkerResult wr) {
        try {
            List<DataPoint> points = new ArrayList<>();
            for (CheckData cd : wr.results) {
                final Map<String, NumericNode> values = new HashMap<>();
                final String timeSeries = "zmon.check." + cd.check_id;

                String worker = "";
                if(cd.check_result.has("worker")) {
                    worker = cd.check_result.get("worker").asText();
                }

                Double ts = cd.check_result.get("ts").asDouble();
                ts = ts * 1000.;
                Long tsL = ts.longValue();

                fillFlatValueMap(values, "", cd.check_result.get("value"));

                for (Map.Entry<String, NumericNode> e : values.entrySet()) {
                    DataPoint p = new DataPoint();
                    p.name = timeSeries;
                    p.tags.put("entity", cd.entity_id.replace("[","_").replace("]","_").replace(":","_").replace("@","_"));

                    for(String field : TAG_FIELDS) {
                        if(cd.entity.containsKey(field)) {
                            String fieldValue = cd.entity.get(field);
                            if(null != fieldValue && !"".equals(fieldValue)) {
                                p.tags.put(field, cd.entity.get(field));
                            }
                        }
                    }

                    if(null!=e.getKey() && !"".equals(e.getKey())) {
                        p.tags.put("key", e.getKey().replace(":", "_").replace("@", "_"));
                    }

                    String metricName = extractMetricName(e.getKey());
                    if(null != metricName) {
                        p.tags.put("metric", metricName.replace(":", "_").replace("@", "_"));
                    }

                    // handle zmon actuator metrics and extract the http status code into its own field
                    // put the first character of the status code into "status group" sg, this is only for easy kairosdb query
                    if(config.getActuatorMetricChecks().contains(cd.check_id)) {
                        final String[] keyParts = e.getKey().split("\\.");

                        if(keyParts.length >= 3 && "health".equals(keyParts[0]) && "200".equals(keyParts[2])) {
                            // remove the 200 health check data points, with 1/sec * instances with elb checks they just confuse
                            continue;
                        }

                        if(keyParts.length >= 3) {
                            final String statusCode = keyParts[keyParts.length - 2];
                            p.tags.put("sc", statusCode);
                            p.tags.put("sg", statusCode.substring(0,1));
                        }
                    }

                    if(null != worker && !"".equals(worker)) {
                        p.tags.put("worker", worker);
                    }

                    ArrayNode arrayNode = mapper.createArrayNode();
                    arrayNode.add(tsL);
                    arrayNode.add(e.getValue());

                    p.datapoints.add(arrayNode);
                    points.add(p);
                }
            }

            String query = mapper.writeValueAsString(points);
            if(config.isLogKairosdbRequests()) {
                LOG.info("KairosDB Query: {}", query);
            }

            executor.execute(Request.Post(this.url).useExpectContinue().bodyString(query,
                    ContentType.APPLICATION_JSON)).returnContent().asString();
        }
        catch(IOException ex) {
            if(config.isLogKairosdbErrors()) {
                LOG.error("KairosDB write failed", ex);
            }
            metrics.markKairosError();
        }
    }
}
