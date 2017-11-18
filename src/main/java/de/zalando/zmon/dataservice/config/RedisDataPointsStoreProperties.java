package de.zalando.zmon.dataservice.config;

public class RedisDataPointsStoreProperties {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 6379;
    private static final int DEFAULT_POOL_SIZE = 20;
    private static final int DEFAULT_TIME_OUT = 500;

    private String host = DEFAULT_HOST;
    private int port = DEFAULT_PORT;
    private int poolSize = DEFAULT_POOL_SIZE;
    private boolean enabled = true;
    private int timeOut = DEFAULT_TIME_OUT;

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(final int poolSize) {
        this.poolSize = poolSize;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(final int timeOut) {
        this.timeOut = timeOut;
    }
}
