package de.zalando.zmon.dataservice.data;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by jmussler on 4/22/15.
 */
public class CheckData {
    public String time;
    public String worker;
    public int check_id;
    public String entity_id;
    public Map<String,String> entity = new HashMap<String,String>();
    public double run_time;
    public JsonNode check_result;
    public boolean exception;
    public Map<String, AlertData> alerts = new HashMap<String,AlertData>(0);
}
