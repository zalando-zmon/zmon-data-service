package de.zalando.zmon.dataservice;

import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;


@RunWith(SpringRunner.class)
public abstract class AbstractControllerTest {

    protected Resource jsonResource(String filename) {
        return new ClassPathResource(filename + ".json", getClass());
    }

    public static String resourceToString(Resource resource) throws IOException {
        return StreamUtils.copyToString(resource.getInputStream(), Charset.defaultCharset());
    }
}
