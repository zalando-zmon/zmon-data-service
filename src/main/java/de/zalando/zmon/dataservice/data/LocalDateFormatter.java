package de.zalando.zmon.dataservice.data;

import java.text.SimpleDateFormat;

/**
 * Created by jmussler on 9/11/15.
 */
class LocalDateFormatter {
    private static final ThreadLocal<SimpleDateFormat> THREAD_LOCAL_DATEFORMAT = new ThreadLocal<SimpleDateFormat>() {
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSXXX");
        }
    };

    static SimpleDateFormat get() {
        return THREAD_LOCAL_DATEFORMAT.get();
    }
}
