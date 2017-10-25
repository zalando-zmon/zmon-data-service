package de.zalando.zmon.dataservice.data;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

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

    private final HttpEventLogger eventLogger;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedisScript<Long> checkAlertScript;

    @Autowired
    public RedisDataStore(JedisPool pool, @DefaultObjectMapper ObjectMapper mapper,
            StringRedisTemplate stringRedisTemplate, HttpEventLogger eventLogger) {
        this.pool = pool;
        this.mapper = mapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.checkAlertScript = initializeScript();
        this.eventLogger = eventLogger;
    }

    private static RedisScript<Long> initializeScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<Long>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/scripts/checkAlerts.lua")));
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    public void storeTrialRun(String requestId, String id, String result) {
        /*
         * final String key = "zmon:trial_run:" + requestId + ":results";
         * BoundHashOperations<Object, String, String> bho =
         * redisTemplate.boundHashOps(key); bho.put(id, result); bho.expire(300,
         * TimeUnit.SECONDS);
         */

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

        if (ad.active && ad.changed) {
            eventLogger.log(ZMonEventType.ALERT_ENTITY_STARTED, checkId, ad.alert_id, checkValue, entity);
        } else if (!ad.active && ad.changed) {
            eventLogger.log(ZMonEventType.ALERT_ENTITY_ENDED, checkId, ad.alert_id, checkValue, entity);
        }
    }

    public void store(WorkerResult wr) {
        for (CheckData cd : wr.results) {
            String checkValue = writeValueAsString(cd.check_result).orElse(EMPTY_CHECK);

            if (null != cd.alerts) {
                for (AlertData alert : cd.alerts.values()) {
                    createEvents(cd.entity_id, cd.check_id, checkValue, alert);
                }
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

        Double alertStart = null;
        if (alert.start_time_ts != null) {
            alertStart = alert.start_time_ts;
        } else if (alert.start_time != null) {
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
