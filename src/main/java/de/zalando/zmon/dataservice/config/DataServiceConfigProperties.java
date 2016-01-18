package de.zalando.zmon.dataservice.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "dataservice")
public class DataServiceConfigProperties {

	private String redisHost = "localhost";
	private int redisPort = 6378;
	private int redisPoolSize = 20;

	private String kairosdbHost = "localhost";
	private int kairosdbPort = 8083;

	private boolean proxyController = false;
	private String proxyControllerUrl = "http://localhost:8080/api/v1/";

	private String proxyControllerBaseUrl = "http://localhost:8080/";

	private boolean proxyScheduler = false;
	private String proxySchedulerUrl = "http://localhost:8085/api/v1/";

	private boolean logCheckData = false;
	private boolean logKairosdbRequests = false;
	private boolean logKairosdbErrors = false;

	private List<Integer> actuatorMetricChecks = new ArrayList<>();

	private List<String> restMetricHosts = new ArrayList<>();
	private int restMetricPort = 8088;

	// @Value("${server.port}")
	private int serverPort;

	private String oauth2TokenInfoUrl;

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

	public String getKairosdbHost() {
		return kairosdbHost;
	}

	public void setKairosdbHost(String kairosdbHost) {
		this.kairosdbHost = kairosdbHost;
	}

	public int getKairosdbPort() {
		return kairosdbPort;
	}

	public void setKairosdbPort(int kairosdbPort) {
		this.kairosdbPort = kairosdbPort;
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

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getOauth2TokenInfoUrl() {
		return oauth2TokenInfoUrl;
	}

	public void setOauth2TokenInfoUrl(String oauth2TokenInfoUrl) {
		this.oauth2TokenInfoUrl = oauth2TokenInfoUrl;
	}

}
