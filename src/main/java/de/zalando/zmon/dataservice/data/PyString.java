package de.zalando.zmon.dataservice.data;

import java.text.ParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PyString {

    private static final Logger LOG = LoggerFactory.getLogger(PyString.class);

    // TODO, what should happen when total shit comes in or null?
    public static Date extractDate(String s) {
        // cut microseconds from string
        // "start_time":"2015-08-10 15:59:02.973108+02:00"

        String startTime = s.substring(0, s.indexOf(".") + 1 + 3) + s.substring(s.lastIndexOf("+"));
        Date date;
        try {
            date = LocalDateFormatter.get().parse(startTime);
        } catch (ParseException e) {
            LOG.error("Could not parse {}", startTime);
            date = new Date();
        }

        return date;
    }

}
