package de.zalando.zmon.dataservice;

import org.junit.rules.ExternalResource;

import org.springframework.util.SocketUtils;
import redis.embedded.RedisServer;
import redis.embedded.RedisServerBuilder;

/**
 * 
 * @author jbellmann
 *
 */
public class RedisServerRule extends ExternalResource {

    private RedisServer redisServer;
    private final int port;

    public RedisServerRule() {
        this(SocketUtils.findAvailableTcpPort());
    }

    public RedisServerRule(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    @Override
    protected void before() throws Throwable {
        redisServer = new RedisServerBuilder().port(port).build();
        redisServer.start();
    }

    @Override
    protected void after() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

}
