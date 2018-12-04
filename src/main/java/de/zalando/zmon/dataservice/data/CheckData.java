package de.zalando.zmon.dataservice.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jmussler on 4/22/15.
 */
public class CheckData {
    public String time;
    public String worker;
    @JsonProperty("check_id")
    public int checkId;
    @JsonProperty("entity_id")
    public String entityId;
    public Map<String,String> entity = new HashMap<>();
    @JsonProperty("run_time")
    public double runTime;
    @JsonProperty("check_result")
    public JsonNode checkResult;
    public boolean exception;
    public Map<String, AlertData> alerts = new HashMap<>(0);
    @JsonProperty("is_sampled")
    public boolean isSampled = true;
}
