package de.zalando.zmon.dataservice;

import java.text.SimpleDateFormat;

/**
 * Created by jmussler on 9/11/15.
 */
public class LocalDateFormatter {
    private static final ThreadLocal<SimpleDateFormat> THREAD_LOCAL_DATEFORMAT = new ThreadLocal<SimpleDateFormat>() {
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSXXX");
        }
    };

    public static SimpleDateFormat get() {
        return THREAD_LOCAL_DATEFORMAT.get();
    }
}
