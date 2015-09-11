package de.zalando.zmon.dataservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.zalando.eventlog.EventLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.text.ParseException;
import java.util.Date;


/**
 * Created by jmussler on 4/22/15.
 */
@Service
public class RedisDataStore  {

    private final JedisPool pool;

    private ObjectMapper mapper = new ObjectMapper();

    private static final Logger LOG = LoggerFactory.getLogger(RedisDataStore.class);

    private static final EventLogger EVENT_LOG = EventLogger.getLogger(RedisDataStore.class);

    @Autowired
    public RedisDataStore(JedisPool pool) {
        this.pool = pool;
    }

    public void storeTrialRun(String requestId, String id, String result) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            String key = "zmon:trial_run:"+requestId+":results";
            jedis.hset(key, id, result);
            jedis.expire(key, 300);
        }
        finally {
            try {
                if(null!=jedis) pool.returnResource(jedis);
            }
            catch(Exception ex) {
                LOG.error("Failed to return Redis to pool in trial run");
            }
        }
    }

    public void createEvents(String entity, int checkId, String checkValue, AlertData ad) {
        if(ad.active && ad.changed) {
            EVENT_LOG.log(ZMonEventType.ALERT_ENTITY_STARTED, checkId, ad.alert_id, checkValue, entity);
        }
        else if(!ad.active && ad.changed) {
            EVENT_LOG.log(ZMonEventType.ALERT_ENTITY_ENDED, checkId, ad.alert_id, checkValue, entity);
        }
    }

    public static Date extractFromPyString(String s) {
        // cut microseconds from string
        // "start_time":"2015-08-10 15:59:02.973108+02:00"

        String startTime = s.substring(0, s.indexOf(".") + 1 + 3)
            + s.substring(s.lastIndexOf("+"));
        LOG.info("{}", startTime);
        Date date;
        try {
            date = LocalDateFormatter.get().parse(startTime);
        } catch (ParseException e) {
            LOG.error("Could not parse {}", startTime);
            date = new Date();
        }

        return date;
    }

    public void store(WorkerResult wr) {
        Jedis jedis = null;

        try {
            jedis = pool.getResource();

            Pipeline p = jedis.pipelined();

            for(CheckData cd : wr.results) {
                p.sadd("zmon:checks", ""+cd.check_id);
                p.sadd("zmon:checks:" + cd.check_id, cd.entity_id);
                String checkTs = "zmon:checks:"+cd.check_id+":"+cd.entity_id;
                String checkValue = "{}";
                try {
                    checkValue = mapper.writeValueAsString(cd.check_result);
                }
                catch(JsonProcessingException ex) {
                    LOG.error("", ex);
                }

                p.lpush(checkTs, checkValue);
                p.ltrim(checkTs, 0, 20);

                if(null!=cd.alerts) {
                    for (AlertData alert : cd.alerts.values()) {

                        createEvents(cd.entity_id, cd.check_id, checkValue, alert);

                        if (alert.active) {
                            p.sadd("zmon:alerts:" + alert.alert_id, cd.entity_id);

                            String value = "{\"value\": \"Serialize failed\"}";
                            ObjectNode vNode = mapper.createObjectNode();
                            vNode.put("captures", alert.captures);
                            vNode.put("downtimes", alert.downtimes);

                            Double alertStart;
                            if(alert.start_time_ts != null) {
                                alertStart = alert.start_time_ts;
                            }
                            else {
                                alertStart = extractFromPyString(alert.start_time).getTime() / 1000.;
                            }

                            vNode.put("start_time", alertStart );

                            vNode.put("ts", cd.check_result.get("ts"));
                            vNode.put("td", cd.check_result.get("td"));
                            vNode.put("worker", cd.check_result.get("worker"));

                            if(cd.exception) {
                                vNode.put("exc", 1);
                            }

                            vNode.putPOJO("value", cd.check_result.get("value"));

                            try {
                                value = mapper.writeValueAsString(vNode);
                            } catch (JsonProcessingException ex) {
                                LOG.error("", ex);
                            }

                            p.set("zmon:alerts:" + alert.alert_id + ":" + cd.entity_id, value);

                        } else {
                            p.srem("zmon:alerts:" + alert.alert_id, cd.entity_id);
                            p.del("zmon:alerts:" + alert.alert_id + ":" + cd.entity_id);
                        }

                        String captures = "{\"msg\":\"ERROR: Captures not serialized\"}";
                        try {
                            captures = mapper.writeValueAsString(alert.captures);
                        } catch (JsonProcessingException ex) {
                            LOG.error("", ex);
                        }

                        p.hset("zmon:alerts:" + alert.alert_id + ":entities", cd.entity_id, captures);

                        p.eval("if table.getn(redis.call('smembers','zmon:alerts:" + alert.alert_id + "')) == 0 then return redis.call('srem','zmon:alerts'," + alert.alert_id + ") else return redis.call('sadd','zmon:alerts',"+alert.alert_id+") end");
                    }
                }
            }
            p.sync();
        }
        finally {
            try {
                if(null!=jedis) pool.returnResource(jedis);
            }
            catch(Exception ex) {
                LOG.error("Redis return to pool failed");
            }
        }
    }
}
