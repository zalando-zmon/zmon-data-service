package de.zalando.zmon.dataservice;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ActiveProfiles;

@SpringApplicationConfiguration(classes={Application.class})
@WebIntegrationTest
@ActiveProfiles("it")
public class DataServiceIT extends AbstractControllerTest {

	@Test
	public void startUp() throws InterruptedException{
		TimeUnit.SECONDS.sleep(5);
	}
}
