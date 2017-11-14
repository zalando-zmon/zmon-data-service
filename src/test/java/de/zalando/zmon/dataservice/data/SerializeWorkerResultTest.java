package de.zalando.zmon.dataservice.data;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SerializeWorkerResultTest {

    private static final Logger log = LoggerFactory.getLogger(RedisDataStore.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void serialize() throws IOException {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String result = mapper.writeValueAsString(Fixture.buildWorkerResult());
        log.info("{}", result);
        //  Return true test - remove later.
    }
}
