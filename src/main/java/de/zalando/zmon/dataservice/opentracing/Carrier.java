package de.zalando.zmon.dataservice.opentracing;

import io.opentracing.propagation.TextMap;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Carrier implements TextMap {

    public HashMap<String, String> map;

    public Carrier(HashMap<String, String> map) {

        this.map = map;
    }

    public void put(String key, String value) {

        map.put(key, value);
    }

    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("TextMapInjectAdapter should only be used with Tracer.inject()");
    }

    public String toString() {
        return map.toString();
    }


}
