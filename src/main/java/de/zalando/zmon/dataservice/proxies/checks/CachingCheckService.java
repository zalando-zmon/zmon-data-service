package de.zalando.zmon.dataservice.proxies.checks;

import de.zalando.zmon.dataservice.TokenWrapper;
import de.zalando.zmon.dataservice.config.DataServiceConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Created by jmussler on 21.03.17.
 */
@Component
public class CachingCheckService implements ChecksService {

    private final Logger log = LoggerFactory.getLogger(CachingCheckService.class);

    private long checkLastModified = 0;
    private long alertsLastModified = 0;

    private String checkLastModifiedString = "";
    private String alertLastModifiedString = "";

    private final ChecksService service;
    private final TokenWrapper wrapper;
    private final DataServiceConfigProperties config;

    private String currentChecks = null;
    private String currentAlerts = null;

    public CachingCheckService(DataServiceConfigProperties config, ChecksService service, TokenWrapper wrapper) {
        this.service = service;
        this.wrapper = wrapper;
        this.config = config;

        try {
            currentChecks = service.allActiveCheckDefinitions(Optional.of(wrapper.get()), "");
            currentAlerts = service.allActiveAlertDefinitions(Optional.of(wrapper.get()), "");
        }
        catch(Throwable t) {

        }
    }

    protected static long getTimestamp(String headerValue) {
        if(headerValue == null) return 0;
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        df.setTimeZone(tz);

        try {
            return df.parse(headerValue).getTime();
        } catch (ParseException e) {
            return 0;
        }
    }

    protected static boolean doRefresh(String headerValue, long currentMaxLastModified, String lastData) {
        if (lastData == null || headerValue == null) return true;

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        df.setTimeZone(tz);

        try {
            long lastModified = df.parse(headerValue).getTime();
            return currentMaxLastModified != lastModified;
        } catch (ParseException e) {
            return true;
        }
    }

    @Scheduled(fixedRate = 60000)
    public void refreshData() {
        try {
            String nextCheckLastModified = service.allActiveCheckDefinitionsLastModified(Optional.of(wrapper.get()), "");

            if(doRefresh(nextCheckLastModified, checkLastModified, currentChecks)) {
                currentChecks = service.allActiveCheckDefinitions(Optional.of(wrapper.get()), "");

                checkLastModified = getTimestamp(nextCheckLastModified);
                checkLastModifiedString = nextCheckLastModified;
            }
        }
        catch(Throwable t) {
            log.error("Unexpected error in loading checks", t);
        }

        try {
            String nextAlertLastModified = service.allActiveAlertDefinitionsLastModified(Optional.of(wrapper.get()), "");

            if(doRefresh(nextAlertLastModified, alertsLastModified, currentAlerts)) {
                currentAlerts = service.allActiveAlertDefinitions(Optional.of(wrapper.get()), "");
                alertsLastModified = getTimestamp(nextAlertLastModified);
                alertLastModifiedString = nextAlertLastModified;
            }
        }
        catch(Throwable t) {
            log.error("Unexpected error in loading alerts", t);
        }
    }

    @Override
    public String allActiveAlertDefinitions(Optional<String> token, String query) throws URISyntaxException, IOException {
        return currentAlerts;
    }

    @Override
    public String allActiveCheckDefinitions(Optional<String> token, String query) throws URISyntaxException, IOException {
        return currentChecks;
    }

    @Override
    public String allActiveAlertDefinitionsLastModified(Optional<String> token, String query) throws URISyntaxException, IOException {
        return alertLastModifiedString;
    }

    @Override
    public String allActiveCheckDefinitionsLastModified(Optional<String> token, String query) throws URISyntaxException, IOException {
        return checkLastModifiedString;
    }
}
