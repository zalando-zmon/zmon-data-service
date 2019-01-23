package de.zalando.zmon.dataservice.proxies.kairosdb;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import de.zalando.zmon.dataservice.data.HttpClientFactory;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
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
import java.util.Optional;

@Controller
@RequestMapping(value = "/kairosdb-proxy/")
public class KairosdbProxy {

    private final Logger log = LoggerFactory.getLogger(KairosdbProxy.class);

    private final Executor executor;

    private final MetricRegistry metricRegistry;
    private final String url;
    private final boolean enabled;

    @Autowired
    public KairosdbProxy(DataServiceConfigProperties config, DataServiceMetrics metrics) {
        this.metricRegistry = metrics.getMetricRegistry();
        this.url = config.getProxyKairosdbUrl();
        this.enabled = this.url != null && !this.url.equals("");

        if (null != url) {
            log.info("KairosDB Proxy: {}", url);
            executor = HttpClientFactory.getExecutor(
                    config.getProxyKairosdbSockettimeout(),
                    config.getProxyKairosdbTimeout(),
                    config.getProxyKairosdbConnections(),
                    config.getConnectionsTimeToLive()
            );
        } else {
            executor = null;
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/v1/datapoints/query"}, method = RequestMethod.POST, produces = "application/json")
    public void kairosDBPost(@RequestBody final JsonNode node, final Writer writer,
                             final HttpServletResponse response) throws IOException {

        final String checkId = getCheckId(node);

        try {
            fixMetricNames(node);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try (Timer.Context ignored = metricRegistry.timer("kairosdb.check.query." + checkId).time()) {
            alignQueriesToMinutes(node);

            final String kairosDBURL = url + "/api/v1/datapoints/query";
            final Request request = Request.Post(kairosDBURL)
                    .addHeader("X-ZMON-CHECK-ID", checkId)
                    .bodyString(node.toString(), ContentType.APPLICATION_JSON);
            proxy(request, writer, response);
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/api/v1/datapoints/query/tags"}, method = RequestMethod.POST, produces = "application/json")
    public void kairosDBtags(@RequestBody final JsonNode node,
                             final Writer writer,
                             final HttpServletResponse response) throws IOException {
        fixMetricNames(node);

        final String kairosDBURL = url + "/api/v1/datapoints/query/tags";
        proxy(Request.Post(kairosDBURL).bodyString(node.toString(), ContentType.APPLICATION_JSON), writer, response);
    }

    @ResponseBody
    @RequestMapping(value = {"/api/v1/metricnames"}, method = RequestMethod.GET, produces = "application/json")
    public void kairosDBmetrics(final Writer writer, final HttpServletResponse response) throws IOException {
        final String kairosDBURL = url + "/api/v1/metricnames";
        proxy(Request.Get(kairosDBURL), writer, response);
    }

    private String getCheckId(final JsonNode node) {
        final JsonNode metrics = node.get("metrics");
        if (metrics.size() > 0) {
            final Optional<JsonNode> nameNode = Optional.ofNullable(metrics.get(0).get("nameNode"));
            final Optional<String> checkId = nameNode.map(n -> n.textValue().replace("zmon.check.", ""));
            return checkId.orElse("");
        }
        return "";
    }


    private void fixMetricNames(final JsonNode node) {
        final JsonNode metrics = node.get("metrics");
        if (metrics == null || !metrics.isArray()) {
            return;
        }

        for (final JsonNode metric : metrics) {
            final Optional<JsonNode> tags = Optional.ofNullable(metric.get("tags"));
            final Optional<JsonNode> keyNode = tags.map(t -> t.get("key")).filter(k -> !k.textValue().isEmpty());
            if (keyNode.isPresent()) {
                final String prefix = metric.get("name").textValue();
                final String suffix = keyNode.get().textValue();
                final String metricName = prefix + "." + suffix;
                ((ObjectNode) metric).put("name", metricName);
                ((ObjectNode) tags.get()).remove("key");
            }
        }
    }

    private void alignQueriesToMinutes(final JsonNode node) {
        if (node.isObject()) {
            final ObjectNode q = (ObjectNode) node;
            q.put("cache_time", 60);
            if (q.has("start_absolute")) {
                long start = q.get("start_absolute").asLong();
                start = start - (start % 60000);
                q.put("start_absolute", start);
            }
        }
    }

    private void proxy(final Request request,
                       final Writer writer,
                       final HttpServletResponse response) throws IOException {
        if (!enabled) {
            writer.write("");
            return;
        }

        try {
            final String data = executor.execute(request).returnContent().toString();
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


}
