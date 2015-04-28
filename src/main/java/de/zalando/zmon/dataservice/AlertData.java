package de.zalando.zmon.dataservice;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by jmussler on 4/22/15.
 */
public class AlertData {
    public int alert_id;
    public JsonNode captures;
    public boolean exception;
    public String start_time;
    public boolean changed;
    public boolean active;
    public JsonNode downtimes;
    public boolean in_period;
}
