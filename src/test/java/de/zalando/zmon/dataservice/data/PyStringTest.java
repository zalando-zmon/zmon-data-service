package de.zalando.zmon.dataservice.data;

import java.util.Date;

import org.assertj.core.api.Assertions;
import org.junit.Test;

//TODO, how to behave on parse-errors ?
public class PyStringTest {

    private static final String EXAMPLE = "2015-08-10 15:59:02.973108+02:00";
    private static final String EXAMPLE_NO_MICRO = "2015-08-10 15:59:02+02:00";

    // not able to parse, but you get a fresh Date
    @Test
    public void noMicros() {
        Date d = PyString.extractDate(EXAMPLE_NO_MICRO);
        Assertions.assertThat(d).isNotNull();
    }

    // not able to parse, throw an exception
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void noAlwaysADate() {
        PyString.extractDate("x");
    }

    @Test
    public void expectedFormat() {
        Date d = PyString.extractDate(EXAMPLE);
        Assertions.assertThat(d).isNotNull();
    }

}
