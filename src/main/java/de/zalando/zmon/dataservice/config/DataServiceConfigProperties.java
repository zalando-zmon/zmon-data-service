package de.zalando.zmon.dataservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "dataservice")
public class DataServiceConfigProperties {

    private String redisHost = "localhost";
    private int redisPort = 6378;
    private int redisPoolSize = 20;

    // Datapoints Redis buffer (queue) for KairosDB
    @NestedConfigurationProperty
    private RedisDataPointsStoreProperties dataPointsStoreProperties = new RedisDataPointsStoreProperties();

    private List<List<String>> kairosdbWriteUrls;
    private List<String> kairosdbTagFields;

    private boolean proxyController = false;
    private boolean proxyControllerCache = true;
    private String proxyControllerUrl = "http://localhost:8080/api/v1/";
    private String proxyControllerBaseUrl = "http://localhost:8080/";
    private boolean proxyControllerOauth2 = false;
    private int proxyControllerConnectTimeout = 1000; // ms
    private int proxyControllerSocketTimeout = 5000; // ms

    private boolean proxyScheduler = false;
    private String proxySchedulerUrl = "http://localhost:8085/api/v1/";

    private boolean logCheckData = false;
    private boolean logKairosdbRequests = false;
    private boolean logKairosdbErrors = false;

    private boolean trackCheckRate = false;
    private boolean writeAllJobMetrics = true;

    private List<Integer> actuatorMetricChecks = new ArrayList<>();

    private List<String> restMetricHosts = new ArrayList<>();
    private int restMetricPort = 8088;
    private int restMetricConnections = 50;
    private int restMetricPoolSize = 50;
    private int restMetricSocketTimeout = 5000;
    private int restMetricTimeout = 1000;

    private Map<String, String> oauth2Scopes = new HashMap<>(0);

    private int kairosdbConnections = 50;
    private int kairosdbTimeout = 1000;
    private int kairosdbSocketTimeout = 10000;
    private boolean kairosdbEnabled = true;

    private String dataProxyUrl = null;
    private int dataProxyConnections = 50;
    private int dataProxyPoolSize = 50;
    private int dataProxySocketTimeout = 5000;
    private int dataProxyTimeout = 1000;

    private boolean eventlogEnabled = false;
    private String eventlogUrl = "http://localhost:8081/";

    private int eventlogConnections = 50;
    private int eventlogPoolSize = 50;
    private int eventlogSocketTimeout = 5000;
    private int eventlogTimeout = 1000;

    private String proxyKairosdbUrl = "";
    private int proxyKairosdbConnections = 25;
    private int proxyKairosdbSocketTimeout = 10000;
    private int proxyKairosdbTimeout = 1000;

    private Integer checkMetricsWatchId = 0;

    private List<Integer> whiteListedChecks = new ArrayList<>();

    public List<Integer> getwhiteListedChecks() {
        return whiteListedChecks;
    }

    public void setwhiteListedChecks(List<Integer> whiteListedChecks) {
        this.whiteListedChecks = whiteListedChecks;
    }


    @NestedConfigurationProperty
    private Map<String, AsyncExecutorProperties> asyncExecutors = new HashMap<>();

    private int resultSizeWarning = 2000;
    private int resultSizeMetricThreshold = 2000;
    private long connectionsTimeToLive = 2 * 60 * 1000;

    private Map<String, Object> versionConfig = null;

    public boolean isTrackCheckRate() {
        return trackCheckRate;
    }

    public void setTrackCheckRate(boolean trackCheckRate) {
        this.trackCheckRate = trackCheckRate;
    }

    public void setWriteAllJobMetrics(boolean writeAllJobMetrics) {
        this.writeAllJobMetrics = writeAllJobMetrics;
    }

    public boolean isWriteAllJobMetrics() {
        return writeAllJobMetrics;
    }

    public Map<String, AsyncExecutorProperties> getAsyncExecutors() {
        return asyncExecutors;
    }

    public void setAsyncExecutors(final Map<String, AsyncExecutorProperties> asyncExecutors) {
        this.asyncExecutors = asyncExecutors;
    }

    public int getResultSizeWarning() {
        return resultSizeWarning;
    }

    public void setResultSizeWarning(int resultSizeWarning) {
        this.resultSizeWarning = resultSizeWarning;
    }

    public int getResultSizeMetricThreshold() {
        return resultSizeMetricThreshold;
    }

    public void setResultSizeMetricThreshold(int resultSizeMetricThreshold) {
        this.resultSizeMetricThreshold = resultSizeMetricThreshold;
    }

    public boolean isProxyControllerOauth2() {
        return proxyControllerOauth2;
    }

    public void setProxyControllerOauth2(boolean proxyControllerOauth2) {
        this.proxyControllerOauth2 = proxyControllerOauth2;
    }

    public boolean isKairosdbEnabled() {
        return kairosdbEnabled;
    }

    public void setKairosdbEnabled(boolean kairosdbEnabled) {
        this.kairosdbEnabled = kairosdbEnabled;
    }

    public int getDataProxyPoolSize() {
        return dataProxyPoolSize;
    }

    public void setDataProxyPoolSize(int dataProxyPoolSize) {
        this.dataProxyPoolSize = dataProxyPoolSize;
    }

    public String getProxyKairosdbUrl() {
        return proxyKairosdbUrl;
    }

    public void setProxyKairosdbUrl(String proxyKairosdbUrl) {
        this.proxyKairosdbUrl = proxyKairosdbUrl;
    }

    public int getProxyKairosdbConnections() {
        return proxyKairosdbConnections;
    }

    public void setProxyKairosdbConnections(int proxyKairosdbConnections) {
        this.proxyKairosdbConnections = proxyKairosdbConnections;
    }

    public int getProxyKairosdbSocketTimeout() {
        return proxyKairosdbSocketTimeout;
    }

    public void setProxyKairosdbSocketTimeout(int proxyKairosdbSocketTimeout) {
        this.proxyKairosdbSocketTimeout = proxyKairosdbSocketTimeout;
    }

    public int getProxyKairosdbTimeout() {
        return proxyKairosdbTimeout;
    }

    public void setProxyKairosdbTimeout(int proxyKairosdbTimeout) {
        this.proxyKairosdbTimeout = proxyKairosdbTimeout;
    }

    public String getDataProxyUrl() {
        return dataProxyUrl;
    }

    public void setDataProxyUrl(String dataProxyUrl) {
        this.dataProxyUrl = dataProxyUrl;
    }

    public int getDataProxyConnections() {
        return dataProxyConnections;
    }

    public void setDataProxyConnections(int dataProxyConnections) {
        this.dataProxyConnections = dataProxyConnections;
    }

    public int getDataProxySocketTimeout() {
        return dataProxySocketTimeout;
    }

    public void setDataProxySocketTimeout(int dataProxySocketTimeout) {
        this.dataProxySocketTimeout = dataProxySocketTimeout;
    }

    public int getDataProxyTimeout() {
        return dataProxyTimeout;
    }

    public void setDataProxyTimeout(int dataProxyTimeout) {
        this.dataProxyTimeout = dataProxyTimeout;
    }

    public int getKairosdbConnections() {
        return kairosdbConnections;
    }

    public void setKairosdbConnections(int kairosdbConnections) {
        this.kairosdbConnections = kairosdbConnections;
    }

    public int getKairosdbTimeout() {
        return kairosdbTimeout;
    }

    public void setKairosdbTimeout(int kairosdbTimeout) {
        this.kairosdbTimeout = kairosdbTimeout;
    }

    public int getKairosdbSocketTimeout() {
        return kairosdbSocketTimeout;
    }

    public void setKairosdbSocketTimeout(int kairosdbSocketTimeout) {
        this.kairosdbSocketTimeout = kairosdbSocketTimeout;
    }

    /**
     * Enables caching for information about a token.
     * <p>
     * Default: false
     */
    private boolean tokenInfoCacheEnabled = false;

    /**
     * Maximum size for the cache.
     * <p>
     * Default : 1000
     */
    private int tokenInfoCacheMaxSize = 1000;

    /**
     * How long these information will be stored in cache. Unit is MINUTES.
     * <p>
     * Default: 240
     */
    private int tokenInfoCacheTime = 240;

    private String oauth2TokenInfoUrl;

    private String oauth2AccessTokenUrl = null;

    private String oauth2StaticToken = "";

    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(int redisPort) {
        this.redisPort = redisPort;
    }

    public int getRedisPoolSize() {
        return redisPoolSize;
    }

    public void setRedisPoolSize(int redisPoolSize) {
        this.redisPoolSize = redisPoolSize;
    }

    public boolean isProxyController() {
        return proxyController;
    }

    public void setProxyController(boolean proxyController) {
        this.proxyController = proxyController;
    }

    public String getProxyControllerUrl() {
        return proxyControllerUrl;
    }

    public int getProxyControllerConnectTimeout() {
        return proxyControllerConnectTimeout;
    }

    public void setProxyControllerConnectTimeout(int proxyControllerConnectTimeout) {
        this.proxyControllerConnectTimeout = proxyControllerConnectTimeout;
    }

    public int getProxyControllerSocketTimeout() {
        return proxyControllerSocketTimeout;
    }

    public void setProxyControllerSocketTimeout(int proxyControllerSocketTimeout) {
        this.proxyControllerSocketTimeout = proxyControllerSocketTimeout;
    }

    public void setProxyControllerUrl(String proxyControllerUrl) {
        this.proxyControllerUrl = proxyControllerUrl;
    }

    public String getProxyControllerBaseUrl() {
        return proxyControllerBaseUrl;
    }

    public void setProxyControllerBaseUrl(String proxyControllerBaseUrl) {
        this.proxyControllerBaseUrl = proxyControllerBaseUrl;
    }

    public boolean isProxyScheduler() {
        return proxyScheduler;
    }

    public void setProxyScheduler(boolean proxyScheduler) {
        this.proxyScheduler = proxyScheduler;
    }

    public String getProxySchedulerUrl() {
        return proxySchedulerUrl;
    }

    public void setProxySchedulerUrl(String proxySchedulerUrl) {
        this.proxySchedulerUrl = proxySchedulerUrl;
    }

    public boolean isLogCheckData() {
        return logCheckData;
    }

    public void setLogCheckData(boolean logCheckData) {
        this.logCheckData = logCheckData;
    }

    public boolean isLogKairosdbRequests() {
        return logKairosdbRequests;
    }

    public void setLogKairosdbRequests(boolean logKairosdbRequests) {
        this.logKairosdbRequests = logKairosdbRequests;
    }

    public boolean isLogKairosdbErrors() {
        return logKairosdbErrors;
    }

    public void setLogKairosdbErrors(boolean logKairosdbErrors) {
        this.logKairosdbErrors = logKairosdbErrors;
    }

    public List<Integer> getActuatorMetricChecks() {
        return actuatorMetricChecks;
    }

    public void setActuatorMetricChecks(List<Integer> actuatorMetricChecks) {
        this.actuatorMetricChecks = actuatorMetricChecks;
    }

    public Integer getCheckMetricsWatchId() {
        return checkMetricsWatchId;
    }

    public void setCheckMetricsWatchId(Integer c) {
        this.checkMetricsWatchId = c;
    }

    public List<String> getRestMetricHosts() {
        return restMetricHosts;
    }

    public void setRestMetricHosts(List<String> restMetricHosts) {
        this.restMetricHosts = restMetricHosts;
    }

    public int getRestMetricPort() {
        return restMetricPort;
    }

    public void setRestMetricPort(int restMetricPort) {
        this.restMetricPort = restMetricPort;
    }

    public int getRestMetricConnections() {
        return restMetricConnections;
    }

    public void setRestMetricConnections(int restMetricConnections) {
        this.restMetricConnections = restMetricConnections;
    }

    public int getRestMetricPoolSize() {
        return restMetricPoolSize;
    }

    public void setRestMetricPoolSize(int restMetricPoolSize) {
        this.restMetricPoolSize = restMetricPoolSize;
    }

    public int getRestMetricSocketTimeout() {
        return restMetricSocketTimeout;
    }

    public void setRestMetricSocketTimeout(int restMetricSocketTimeout) {
        this.restMetricSocketTimeout = restMetricSocketTimeout;
    }

    public int getRestMetricTimeout() {
        return restMetricTimeout;
    }

    public void setRestMetricTimeout(int restMetricTimeout) {
        this.restMetricTimeout = restMetricTimeout;
    }

    public String getOauth2TokenInfoUrl() {
        return oauth2TokenInfoUrl;
    }

    public void setOauth2TokenInfoUrl(String oauth2TokenInfoUrl) {
        this.oauth2TokenInfoUrl = oauth2TokenInfoUrl;
    }

    public Map<String, String> getOauth2Scopes() {
        return oauth2Scopes;
    }

    public void setOauth2Scopes(Map<String, String> oauth2Scopes) {
        this.oauth2Scopes = oauth2Scopes;
    }

    public int getTokenInfoCacheMaxSize() {
        return tokenInfoCacheMaxSize;
    }

    public void setTokenInfoCacheMaxSize(int tokenInfoCacheMaxSize) {
        this.tokenInfoCacheMaxSize = tokenInfoCacheMaxSize;
    }

    public int getTokenInfoCacheTime() {
        return tokenInfoCacheTime;
    }

    public void setTokenInfoCacheTime(int tokenInfoCacheTime) {
        this.tokenInfoCacheTime = tokenInfoCacheTime;
    }

    public boolean isTokenInfoCacheEnabled() {
        return tokenInfoCacheEnabled;
    }

    public void setTokenInfoCacheEnabled(boolean tokenInfoCacheEnabled) {
        this.tokenInfoCacheEnabled = tokenInfoCacheEnabled;
    }

    public List<List<String>> getKairosdbWriteUrls() {
        return kairosdbWriteUrls;
    }

    public void setKairosdbWriteUrls(List<List<String>> kairosdbWriteUrls) {
        this.kairosdbWriteUrls = kairosdbWriteUrls;
    }

    public int getEventlogConnections() {
        return eventlogConnections;
    }

    public void setEventlogConnections(int eventlogConnections) {
        this.eventlogConnections = eventlogConnections;
    }

    public int getEventlogPoolSize() {
        return eventlogPoolSize;
    }

    public void setEventlogPoolSize(int eventlogPoolSize) {
        this.eventlogPoolSize = eventlogPoolSize;
    }

    public int getEventlogSocketTimeout() {
        return eventlogSocketTimeout;
    }

    public void setEventlogSocketTimeout(int eventlogSocketTimeout) {
        this.eventlogSocketTimeout = eventlogSocketTimeout;
    }

    public int getEventlogTimeout() {
        return eventlogTimeout;
    }

    public void setEventlogTimeout(int eventlogTimeout) {
        this.eventlogTimeout = eventlogTimeout;
    }

    public boolean isEventlogEnabled() {
        return eventlogEnabled;
    }

    public void setEventlogEnabled(boolean eventlogEnabled) {
        this.eventlogEnabled = eventlogEnabled;
    }

    public String getEventlogUrl() {
        return eventlogUrl;
    }

    public void setEventlogUrl(String eventlogUrl) {
        this.eventlogUrl = eventlogUrl;
    }

    public Map<String, Object> getVersionConfig() {
        return versionConfig;
    }

    public void setVersionConfig(Map<String, Object>  versionConfig) {
        this.versionConfig = versionConfig;
    }

    public boolean isProxyControllerCache() {
        return proxyControllerCache;
    }

    public void setProxyControllerCache(boolean proxyControllerCache) {
        this.proxyControllerCache = proxyControllerCache;
    }

    public String getOauth2AccessTokenUrl() {
        return oauth2AccessTokenUrl;
    }

    public void setOauth2AccessTokenUrl(String oauth2AccessTokenUrl) {
        this.oauth2AccessTokenUrl = oauth2AccessTokenUrl;
    }

    public String getOauth2StaticToken() {
        return oauth2StaticToken;
    }

    public void setOauth2StaticToken(String oauth2StaticToken) {
        this.oauth2StaticToken = oauth2StaticToken;
    }

    public List<String> getKairosdbTagFields() {
        return kairosdbTagFields;
    }

    public void setKairosdbTagFields(List<String> kairosdbTagFields) {
        this.kairosdbTagFields = kairosdbTagFields;
    }

    public RedisDataPointsStoreProperties getDataPointsStoreProperties() {
        return dataPointsStoreProperties;
    }

    public void setDataPointsStoreProperties(final RedisDataPointsStoreProperties dataPointsStoreProperties) {
        this.dataPointsStoreProperties = dataPointsStoreProperties;
    }

    public long getConnectionsTimeToLive() {
        return connectionsTimeToLive;
    }

    public void setConnectionsTimeToLive(long connectionsTimeToLive) {
        this.connectionsTimeToLive = connectionsTimeToLive;
    }
}

