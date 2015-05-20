package de.zalando.zmon.dataservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;


/**
 * Created by jmussler on 4/22/15.
 */
@Service
public class RedisDataStore  {

    private final JedisPool pool;

    private ObjectMapper mapper = new ObjectMapper();

    private static final Logger LOG = LoggerFactory.getLogger(RedisDataStore.class);

    @Autowired
    public RedisDataStore(JedisPool pool) {
        this.pool = pool;
    }

    public void store(WorkerResult wr) {
        Jedis jedis = null;
        Jedis alertStateJedis = null;

        try {
            jedis = pool.getResource();
            alertStateJedis = pool.getResource();

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
                        if (alert.active) {
                            p.sadd("zmon:alerts:" + alert.alert_id, cd.entity_id);

                            String value = "{\"value\": \"Serialize failed\"}";
                            ObjectNode vNode = mapper.createObjectNode();
                            vNode.put("captures", alert.captures);
                            vNode.put("downtimes", alert.downtimes);
                            vNode.put("start_time", alert.start_time);

                            vNode.put("ts", cd.check_result.get("ts"));
                            vNode.put("td", cd.check_result.get("td"));

                            if(cd.exception) {
                                vNode.put("exc", 1);
                            }

                            vNode.putPOJO("value", cd.check_result);

                            try {
                                value = mapper.writeValueAsString(vNode);
                            } catch (JsonProcessingException ex) {

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

                        p.eval("if table.getn(redis.call('smembers','zmon:alerts:" + alert.alert_id + "')) == 0 then return redis.call('srem','zmon:alerts'," + alert.alert_id + ") else return redis.call('sadd','zmon:alerts',\"+alert.alert_id+\") end");
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
            }

            try {
                if(null!=alertStateJedis) pool.returnResource(alertStateJedis);
            }
            catch(Exception ex) {
            }
        }
    }
}
