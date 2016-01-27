package de.zalando.zmon.dataservice;

import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.util.StreamUtils;

public abstract class AbstractControllerTest {
	
	@Rule
	public SpringMethodRule methodRule = new SpringMethodRule();

	@ClassRule
	public static final SpringClassRule clazzRule = new SpringClassRule();

	protected Resource jsonResource(String filename) {
		return new ClassPathResource(filename + ".json", getClass());
	}

	public static String resourceToString(Resource resource) throws IOException {
		return StreamUtils.copyToString(resource.getInputStream(), Charset.defaultCharset());
	}
}
