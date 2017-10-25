package de.zalando.zmon.dataservice.config;

public class AsyncExecutorProperties {
    public static final int DEFAULT_CORE_SIZE = 150;
    public static final int DEFAULT_MAX_SIZE = 200;
    public static final int DEFAULT_QUEUE_SIZE = 5_000;
    public static final AsyncExecutorProperties DEFAULT = new AsyncExecutorProperties();

    private int coreSize = DEFAULT_CORE_SIZE;
    private int maxSize = DEFAULT_MAX_SIZE;
    private int queueSize = DEFAULT_QUEUE_SIZE;

    public int getCoreSize() {
        return coreSize;
    }

    public void setCoreSize(final int coreSize) {
        this.coreSize = coreSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(final int maxSize) {
        this.maxSize = maxSize;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(final int queueSize) {
        this.queueSize = queueSize;
    }
}
