package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by jmussler on 4/22/15.
 */
public class AlertData {
    public int alert_id;
    public JsonNode captures;
    public boolean exception;
    public String start_time;
    public Double start_time_ts;
    public boolean changed;
    public boolean active;
    public JsonNode downtimes;
    public boolean in_period;
    public Double alert_evaluation_ts;
}
