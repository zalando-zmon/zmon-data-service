package de.zalando.zmon.dataservice.data;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.zalando.eventlog.EventLogger;
import de.zalando.zmon.dataservice.ZMonEventType;
import de.zalando.zmon.dataservice.components.DefaultObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

/**
 * Created by jmussler on 4/22/15.
 */
@Service
public class RedisDataStore {

    private static final String CAPTURES_NOT_SERIALIZED = "{\"msg\":\"ERROR: Captures not serialized\"}";

    private static final String EMPTY_CHECK = "{}";

    private static final String SERIALIZE_FAILED = "{\"value\": \"Serialize failed\"}";

    private final JedisPool pool;

    private final ObjectMapper mapper;

    private static final Logger LOG = LoggerFactory.getLogger(RedisDataStore.class);

    private static final EventLogger EVENT_LOG = EventLogger.getLogger(RedisDataStore.class);

    private final RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    public RedisDataStore(JedisPool pool, @DefaultObjectMapper ObjectMapper mapper,
            RedisTemplate<Object, Object> redisTemplate) {
        this.pool = pool;
        this.mapper = mapper;
        this.redisTemplate = redisTemplate;
    }

    public void storeTrialRun(String requestId, String id, String result) {
        final String key = "zmon:trial_run:" + requestId + ":results";
        BoundHashOperations<Object, String, String> bho = redisTemplate.boundHashOps(key);
        bho.put(id, result);
        bho.expire(300, TimeUnit.SECONDS);
        // before was
        // Jedis jedis = null;
        // try {
        // jedis = pool.getResource();
        // // String key = "zmon:trial_run:" + requestId + ":results";
        // jedis.hset(key, id, result);
        // jedis.expire(key, 300);
        // } finally {
        // try {
        // if (null != jedis)
        // pool.returnResource(jedis);
        // } catch (Exception ex) {
        // LOG.error("Failed to return Redis to pool in trial run");
        // }
        // }
    }

    public void createEvents(String entity, int checkId, String checkValue, AlertData ad) {
        if (ad.active && ad.changed) {
            EVENT_LOG.log(ZMonEventType.ALERT_ENTITY_STARTED, checkId, ad.alert_id, checkValue, entity);
        } else if (!ad.active && ad.changed) {
            EVENT_LOG.log(ZMonEventType.ALERT_ENTITY_ENDED, checkId, ad.alert_id, checkValue, entity);
        }
    }

    public void store(WorkerResult wr) {
        Jedis jedis = null;

        try {
            jedis = pool.getResource();

            Pipeline p = jedis.pipelined();

            for (CheckData cd : wr.results) {
                p.sadd("zmon:checks", "" + cd.check_id);
                p.sadd("zmon:checks:" + cd.check_id, cd.entity_id);
                String checkTs = "zmon:checks:" + cd.check_id + ":" + cd.entity_id;

                String checkValue = writeValueAsString(cd.check_result).orElse(EMPTY_CHECK);
                p.lpush(checkTs, checkValue);
                p.ltrim(checkTs, 0, 20);

                if (null != cd.alerts) {
                    for (AlertData alert : cd.alerts.values()) {

                        createEvents(cd.entity_id, cd.check_id, checkValue, alert);

                        if (alert.active) {
                            p.sadd("zmon:alerts:" + alert.alert_id, cd.entity_id);

                            String value = buildValue(alert, cd);

                            p.set("zmon:alerts:" + alert.alert_id + ":" + cd.entity_id, value);

                        } else {
                            p.srem("zmon:alerts:" + alert.alert_id, cd.entity_id);
                            p.del("zmon:alerts:" + alert.alert_id + ":" + cd.entity_id);
                        }

                        String captures = writeValueAsString(alert.captures).orElse(CAPTURES_NOT_SERIALIZED);

                        p.hset("zmon:alerts:" + alert.alert_id + ":entities", cd.entity_id, captures);

                        p.eval("if table.getn(redis.call('smembers','zmon:alerts:" + alert.alert_id
                                + "')) == 0 then return redis.call('srem','zmon:alerts'," + alert.alert_id
                                + ") else return redis.call('sadd','zmon:alerts'," + alert.alert_id + ") end");
                    }
                }
            }
            p.sync();
        } finally {
            try {
                if (null != jedis) {
                    jedis.close();
                }
            } catch (Exception ex) {
                LOG.error("Redis return to pool failed", ex);
            }
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

        Double alertStart;
        if (alert.start_time_ts != null) {
            alertStart = alert.start_time_ts;
        } else {
            alertStart = PyString.extractDate(alert.start_time).getTime() / 1000.;
        }

        vNode.put("start_time", alertStart);

        vNode.set("ts", cd.check_result.get("ts"));
        vNode.set("td", cd.check_result.get("td"));
        vNode.set("worker", cd.check_result.get("worker"));

        if (cd.exception) {
            vNode.put("exc", 1);
        }

        vNode.putPOJO("value", cd.check_result.get("value"));

        try {
            value = mapper.writeValueAsString(vNode);
        } catch (JsonProcessingException ex) {
            LOG.error("", ex);
        }
        return value;
    }
}
