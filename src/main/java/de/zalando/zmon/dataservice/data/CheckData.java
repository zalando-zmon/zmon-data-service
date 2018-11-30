package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jmussler on 4/22/15.
 */
public class CheckData {
    public String time;
    public String worker;
    public int check_id;
    public String entity_id;
    public Map<String,String> entity = new HashMap<>();
    public double run_time;
    public JsonNode check_result;
    public boolean exception;
    public Map<String, AlertData> alerts = new HashMap<>(0);
    public boolean is_sampled = true;
}
