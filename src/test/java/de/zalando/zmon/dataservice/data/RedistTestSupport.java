package de.zalando.zmon.dataservice.data;

import de.zalando.zmon.dataservice.RedisServerRule;
import org.junit.ClassRule;

public class RedistTestSupport {
    @ClassRule
    public static final RedisServerRule REDIS_SERVER = new RedisServerRule();
}
