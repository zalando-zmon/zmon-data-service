package de.zalando.zmon.dataservice.proxies.kairosdb;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

@Controller
@RequestMapping(value = "/kairosdb-proxy/")
public class KairosdbProxy {

    private final Executor executor;

    private final DataServiceConfigProperties config;

    private final DataServiceMetrics metrics;
    private final MetricRegistry metricRegistry;
    private final String url;
    private final boolean enabled;

    public static HttpClient getHttpClient(int socketTimeout, int timeout, int maxConnections) {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(timeout).build();
        return HttpClients.custom().setMaxConnPerRoute(maxConnections).setMaxConnTotal(maxConnections).setDefaultRequestConfig(config).build();
    }

    @Autowired
    public KairosdbProxy(DataServiceConfigProperties config, DataServiceMetrics metrics) {
        this.config = config;
        this.metrics = metrics;
        this.metricRegistry = metrics.getMetricRegistry();
        this.url = config.getProxyKairosdbUrl();
        this.enabled = this.url!=null && !this.url.equals("");

        if (null != url) {
            executor = Executor.newInstance(getHttpClient(config.getProxyKairosdbSockettimeout(), config.getProxyKairosdbTimeout(), config.getProxyKairosdbConnections()));
        }
        else {
            executor = null;
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/v1/datapoints/query"}, method = RequestMethod.POST, produces = "application/json")
    public void kairosDBPost(@RequestBody(required = true) final JsonNode node, final Writer writer,
                             final HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        if (!enabled) {
            writer.write("");
            return;
        }

        final String checkId = node.get("metrics").get(0).get("name").textValue().replace("zmon.check.", "");
        Timer.Context timer = metricRegistry.timer("kairosdb.check.query." + checkId).time();

        // align all queries to full minutes
        if (node instanceof ObjectNode) {
            ObjectNode q = (ObjectNode) node;
            q.put("cache_time", 60);
            if (q.has("start_absolute")) {
                long start = q.get("start_absolute").asLong();
                start = start - (start % 60000);
                q.put("start_absolute", start);
            }
        }

        final String kairosDBURL = url + "/api/v1/datapoints/query";

        final String r = executor.execute(Request.Post(kairosDBURL)
                                                 .addHeader("X-ZMON-CHECK-ID", checkId)
                                                 .addHeader("Cookie", "x-zmon-check-id=" + checkId)
                                                 .bodyString(node.toString(), ContentType.APPLICATION_JSON)).returnContent().asString();

        if (timer != null) {
            timer.stop();
        }

        writer.write(r);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/v1/datapoints/query/tags"}, method = RequestMethod.POST, produces = "application/json")
    public void kairosDBtags(@RequestBody(required = true) final JsonNode node, final Writer writer,
                             final HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        if (!enabled) {
            writer.write("");
            return;
        }

        final String kairosDBURL = url + "/api/v1/datapoints/query/tags";

        final String r = executor.execute(Request.Post(kairosDBURL).useExpectContinue().bodyString(node.toString(),
                ContentType.APPLICATION_JSON)).returnContent().asString();

        writer.write(r);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/v1/metricnames"}, method = RequestMethod.GET, produces = "application/json")
    public void kairosDBmetrics(final Writer writer, final HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        if (!enabled) {
            writer.write("");
            return;
        }

        final String kairosDBURL = url + "/api/v1/metricnames";

        final String r = executor.execute(Request.Get(kairosDBURL).useExpectContinue()).returnContent().asString();

        writer.write(r);
    }
}
