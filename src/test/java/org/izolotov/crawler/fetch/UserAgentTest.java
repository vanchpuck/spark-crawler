package org.izolotov.crawler.fetch;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class UserAgentTest {

    @Test
    public void userAgentStringTest() {
        UserAgent userAgent = new UserAgent("TestAgent", "test@agent.com");
        assertThat(userAgent.getUserAgentString(), is("TestAgent (test@agent.com)"));
    }

    @Test
    public void noInfoUserAgentStringTest() {
        UserAgent userAgent = new UserAgent("TestAgent");
        assertThat(userAgent.getUserAgentString(), is("TestAgent"));
    }

}
