package de.zalando.zmon.dataservice.proxies.kairosdb;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.apache.http.HttpException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger log = LoggerFactory.getLogger(KairosdbProxy.class);

    private final Executor executor;

    private final MetricRegistry metricRegistry;
    private final String url;
    private final boolean enabled;

    public static HttpClient getHttpClient(int socketTimeout, int timeout, int maxConnections) {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(timeout).build();
        return HttpClients.custom().setMaxConnPerRoute(maxConnections).setMaxConnTotal(maxConnections).setDefaultRequestConfig(config).build();
    }

    @Autowired
    public KairosdbProxy(DataServiceConfigProperties config, DataServiceMetrics metrics) {
        this.metricRegistry = metrics.getMetricRegistry();
        this.url = config.getProxyKairosdbUrl();
        this.enabled = this.url != null && !this.url.equals("");

        if (null != url) {
            log.info("KairosDB Proxy: {}", url);
            executor = Executor.newInstance(getHttpClient(config.getProxyKairosdbSockettimeout(), config.getProxyKairosdbTimeout(), config.getProxyKairosdbConnections()));
        } else {
            executor = null;
        }
    }

    private void proxy(Request request, Writer writer, HttpServletResponse response) throws IOException {
        if (!enabled) {
            writer.write("");
            return;
        }

        try {
            String data = executor.execute(request).returnContent().toString();
            response.setContentType("application/json");
            writer.write(data);
        } catch (HttpResponseException hre) {
            log.warn("KairosDB returned non 2xx response: {}", hre.getMessage());
            response.sendError(hre.getStatusCode());
        } catch (IOException e) {
            log.warn("I/O error while calling KairosDB: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY);
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/v1/datapoints/query"}, method = RequestMethod.POST, produces = "application/json")
    public void kairosDBPost(@RequestBody(required = true) final JsonNode node, final Writer writer,
                             final HttpServletResponse response) throws IOException {

        String metricName = null;
        try {
            metricName = node.get("metrics").get(0).get("name").textValue();
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (!metricName.startsWith("zmon.check.")) {
            response.setContentType("application/json");
            writer.write("{}");
            return;
        }

        final String checkId = metricName.replace("zmon.check.", "");

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

        proxy(Request.Post(kairosDBURL)
                .addHeader("X-ZMON-CHECK-ID", checkId)
                .bodyString(node.toString(), ContentType.APPLICATION_JSON), writer, response
        );

        if (timer != null) {
            timer.stop();
        }

    }

    @ResponseBody
    @RequestMapping(value = {"/api/v1/datapoints/query/tags"}, method = RequestMethod.POST, produces = "application/json")
    public void kairosDBtags(@RequestBody(required = true) final JsonNode node, final Writer writer,
                             final HttpServletResponse response) throws IOException {
        final String kairosDBURL = url + "/api/v1/datapoints/query/tags";

        proxy(Request.Post(kairosDBURL).bodyString(node.toString(), ContentType.APPLICATION_JSON), writer, response);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/v1/metricnames"}, method = RequestMethod.GET, produces = "application/json")
    public void kairosDBmetrics(final Writer writer, final HttpServletResponse response) throws IOException {
        final String kairosDBURL = url + "/api/v1/metricnames";

        proxy(Request.Get(kairosDBURL), writer, response);
    }
}
