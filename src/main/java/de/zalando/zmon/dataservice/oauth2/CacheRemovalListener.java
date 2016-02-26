package de.zalando.zmon.dataservice.oauth2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * Only logs removal from cache.
 * 
 * @author jbellmann
 *
 */
class CacheRemovalListener implements RemovalListener<String, OAuth2Authentication> {

    private final Logger log = LoggerFactory.getLogger(CacheRemovalListener.class);

    @Override
    public void onRemoval(RemovalNotification<String, OAuth2Authentication> notification) {
        if (log.isDebugEnabled()) {
            log.debug("Remove Authentication for token : {}", notification.getKey().substring(0, 6));
        }
    }

}