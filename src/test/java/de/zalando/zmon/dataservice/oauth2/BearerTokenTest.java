package de.zalando.zmon.dataservice.oauth2;

import org.apache.http.client.fluent.Request;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

public class BearerTokenTest {

    @Test
    public void inject() {
        Request request = Mockito.mock(Request.class);
        BearerToken.inject(request, Optional.of("123"));
        Mockito.verify(request).addHeader("Authorization", "Bearer 123");
    }
}
