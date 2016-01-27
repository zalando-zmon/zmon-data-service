package de.zalando.zmon.dataservice.data;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class SerializeWorkerResultTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void serialize() throws IOException {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(System.out, Fixture.buildWorkerResult());
    }
}
