package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.zalando.zmon.dataservice.DataServiceMetrics;
import de.zalando.zmon.dataservice.ZMonEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.util.Optional;

public class RedisDataStore {

    private static final String CAPTURES_NOT_SERIALIZED = "{\"msg\":\"ERROR: Captures not serialized\"}";

    private static final String EMPTY_CHECK = "{}";

    private static final String SERIALIZE_FAILED = "{\"value\": \"Serialize failed\"}";

    private final JedisPool pool;

    private final ObjectMapper mapper;

    private static final Logger LOG = LoggerFactory.getLogger(RedisDataStore.class);

    private final HttpEventLogger eventLogger;

    private final RedisScript<Long> checkAlertScript;

    private final DataServiceMetrics metrics;

    public RedisDataStore(final JedisPool pool, final ObjectMapper mapper,
                          final HttpEventLogger eventLogger, DataServiceMetrics metrics) {
        this.pool = pool;
        this.mapper = mapper;
        this.checkAlertScript = initializeScript();
        this.eventLogger = eventLogger;
        this.metrics = metrics;
    }

    private static RedisScript<Long> initializeScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<Long>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/scripts/checkAlerts.lua")));
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    public void storeTrialRun(String requestId, String id, String result) {
        try (Jedis jedis = pool.getResource()){
            String key = "zmon:trial_run:" + requestId + ":results";
            jedis.hset(key, id, result);
            jedis.expire(key, 300);
        }
    }

    public void createEvents(String entity, int checkId, String checkValue, AlertData ad) {
        if (eventLogger == null) {
            return;
        }

        String captures = writeValueAsString(ad.captures).orElse(EMPTY_CHECK);
        if (ad.active && ad.changed) {
            eventLogger.log(ZMonEventType.ALERT_ENTITY_STARTED, checkId, ad.alert_id, checkValue, entity, captures);
        } else if (!ad.active && ad.changed) {
            eventLogger.log(ZMonEventType.ALERT_ENTITY_ENDED, checkId, ad.alert_id, checkValue, entity, captures);
        }
    }

    public void store(WorkerResult wr) {
        try (Jedis jedis = pool.getResource()){

            Pipeline p = jedis.pipelined();

            for (CheckData cd : wr.results) {
                p.sadd("zmon:checks", "" + cd.checkId);
                p.sadd("zmon:checks:" + cd.checkId, cd.entityId);
                String checkTs = "zmon:checks:" + cd.checkId + ":" + cd.entityId;

                String checkValue = writeValueAsString(cd.checkResult).orElse(EMPTY_CHECK);
                p.lpush(checkTs, checkValue);
                p.ltrim(checkTs, 0, 2);

                if (null != cd.alerts) {
                    for (AlertData alert : cd.alerts.values()) {

                        createEvents(cd.entityId, cd.checkId, checkValue, alert);

                        if (alert.active && alert.in_period) {
                            p.sadd("zmon:alerts:" + alert.alert_id, cd.entityId);

                            String value = buildValue(alert, cd);

                            p.set("zmon:alerts:" + alert.alert_id + ":" + cd.entityId, value);

                        } else {
                            p.srem("zmon:alerts:" + alert.alert_id, cd.entityId);
                            p.del("zmon:alerts:" + alert.alert_id + ":" + cd.entityId);
                        }

                        String captures = writeValueAsString(alert.captures).orElse(CAPTURES_NOT_SERIALIZED);

                        p.hset("zmon:alerts:" + alert.alert_id + ":entities", cd.entityId, captures);

                        p.eval("if redis.call('scard','zmon:alerts:" + alert.alert_id + "') == 0 then " +
                                    "redis.call('srem','zmon:alert-acks', " + alert.alert_id + "); " +
                                    "return redis.call('srem','zmon:alerts'," + alert.alert_id + ") " +
                                "else " +
                                    "return redis.call('sadd','zmon:alerts'," + alert.alert_id + ") " +
                                "end");

                        if (alert.alert_evaluation_ts != null) {
                            Double duration = Math.abs(System.currentTimeMillis() - alert.alert_evaluation_ts * 1000.);
                            metrics.updateAlertDurations(duration.longValue());
                        }
                    }
                }
            }
            p.sync();
        }
    }

    protected Optional<String> writeValueAsString(JsonNode node) {
        try {
            return Optional.ofNullable(mapper.writeValueAsString(node));
        } catch (JsonProcessingException ex) {
            LOG.error("", ex);
            return Optional.empty();
        }
    }

    protected String buildValue(AlertData alert, CheckData cd) {
        String value = SERIALIZE_FAILED;
        ObjectNode vNode = mapper.createObjectNode();
        vNode.set("captures", alert.captures);
        vNode.set("downtimes", alert.downtimes);

        Double alertStart = null;
        if (alert.start_time_ts != null) {
            alertStart = alert.start_time_ts;
        } else if (alert.start_time != null) {
            alertStart = PyString.extractDate(alert.start_time).getTime() / 1000.;
        }

        vNode.put("start_time", alertStart);

        vNode.set("ts", cd.checkResult.get("ts"));
        vNode.set("td", cd.checkResult.get("td"));
        vNode.set("worker", cd.checkResult.get("worker"));

        if (cd.exception) {
            vNode.put("exc", 1);
        }

        vNode.putPOJO("value", cd.checkResult.get("value"));

        try {
            value = mapper.writeValueAsString(vNode);
        } catch (JsonProcessingException ex) {
            LOG.error("", ex);
        }
        return value;
    }
}
