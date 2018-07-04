package de.zalando.zmon.dataservice.opentracing;

import io.opentracing.propagation.TextMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Carrier implements TextMap {

    public HashMap<String, String> map;

    public Carrier() {

        map = new HashMap<String, String>();
    }

    public Carrier(HashMap<String, String> map) {

        this.map = map;
    }

    public void put(String key, String value) {

        map.put(key, value);
    }

    public Iterator<Map.Entry<String, String>> iterator() {
        return map.entrySet().iterator();
    }

    public String toString() {
        return map.toString();
    }


}
