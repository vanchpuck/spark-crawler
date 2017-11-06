package org.izolotov.crawler.fetch;

import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class DelayFetcherTest {

    public static final int PORT = 8081;
    public static final String USER_AGENT_STR = "TestAgent";

    private static final String TEXT_PLAIN = "text/plain;charset=utf-8";

    private static final String URL_USER_AGENT_1 = String.format("http://localhost:%d/user_agent_1", PORT);
    private static final String URL_USER_AGENT_2 = String.format("http://localhost:%d/user_agent_2", PORT);
    private static final String URL_USER_AGENT_3 = String.format("http://localhost:%d/user_agent_3", PORT);
    private static final String URL_DELAY_1= String.format("http://localhost:%d/delay_1", PORT);
    private static final String URL_DELAY_2 = String.format("http://localhost:%d/delay_2", PORT);
    private static final String URL_EXCEPTION_1 = String.format("http://localhost:%d/exception_1", PORT);
    private static final String URL_EXCEPTION_2 = String.format("http://localhost:%d/exception_2", PORT);
    private static final String URL_NO_SUCH_PAGE_1 = String.format("http://localhost:%d/no_such_page_1", PORT);
    private static final String URL_NO_SUCH_PAGE_2 = String.format("http://localhost:%d/no_such_page_2", PORT);
    private static final String URL_CONNECTION_REFUSED = "http://localhost:55555/conn_refused.html";
    private static final String URL_UNKNOWN_HOST = "http://___unknown__host___:61217/unknown.html";
    private static final String URL_MALFORMED = String.format("^^:*#!?,.localhost:%d/malformed.html", PORT);

    private static Server server;
    private static UserAgent userAgent;
    private static CloseableHttpClient httpClient;

    public static class ResponseHandler extends AbstractHandler {

        public static final long DELAY = 2000L;

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            final String requestUrl = baseRequest.getRequestURL().toString();
            if (URL_USER_AGENT_1.equals(requestUrl) || URL_USER_AGENT_2.equals(requestUrl) || URL_USER_AGENT_3.equals(requestUrl)) {
                returnUserAgent(baseRequest, response);
            } else if (URL_DELAY_1.equals(requestUrl) || URL_DELAY_2.equals(requestUrl)) {
                try {
                    Thread.sleep(DELAY);
                    returnUserAgent(baseRequest, response);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else if (URL_EXCEPTION_1.equals(requestUrl) || URL_EXCEPTION_2.equals(requestUrl)) {
                throw new RuntimeException("Internal error!");
            }
        }

        private void returnUserAgent(Request baseRequest, HttpServletResponse response) throws IOException {
            response.setContentType(TEXT_PLAIN);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().print(baseRequest.getHeader(HttpHeader.USER_AGENT.toString()));
            baseRequest.setHandled(true);
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {
        userAgent = new UserAgent(USER_AGENT_STR);
        httpClient = HttpClients.custom()
                .setUserAgent(userAgent.getUserAgentString())
                .build();
        server = new Server(PORT);
        server.setHandler(new ResponseHandler());
        server.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testResponse() throws Exception {
        DelayFetcher fetcher = new DelayFetcher(httpClient, 1000L);
        for (String url : Arrays.asList(URL_USER_AGENT_1, URL_DELAY_1, URL_USER_AGENT_2, URL_DELAY_2)) {
            try(CloseableHttpResponse response = fetcher.fetch(url).getResponse()) {
                assertThat("Wrong HTTP status code", ResponseUtil.getHttpStatusCode(response), is(HttpStatus.SC_OK));
                assertThat("Wrong response content", ResponseUtil.getContentString(response, StandardCharsets.UTF_8), is(USER_AGENT_STR));
                assertThat("Wrong response content type", ResponseUtil.getContentType(response), is(TEXT_PLAIN));
            }
        }
    }

    @Test
    public void responseTimeTest() throws Exception {
        DelayFetcher fetcher = new DelayFetcher(httpClient);
        long startTime = System.currentTimeMillis();
        FetchResult<CloseableHttpResponse> summary = fetcher.fetch(URL_DELAY_1);
        long elapsedTime = System.currentTimeMillis() - startTime;
        assertThat("Response time should be greater than Jetty delay",
                summary.getResponseTime(),  allOf(lessThan(elapsedTime), greaterThan(ResponseHandler.DELAY)));
        summary.getResponse().close();
    }

    @Test
    public void firstFetchNoDelayTest() throws Exception {
        final long delay = 3000L;
        DelayFetcher fetcher = new DelayFetcher(httpClient, delay);
        long startTime = System.currentTimeMillis();
        FetchResult<CloseableHttpResponse> summary = fetcher.fetch(URL_USER_AGENT_1);
        long elapsedTime = System.currentTimeMillis() - startTime;
        assertThat("There must be no delay on first fetch", elapsedTime, lessThan(delay));
        summary.getResponse().close();
    }

    @Test
    public void delayTest() throws Exception {
        DelayFetcher fetcher = new DelayFetcher(httpClient);
        long currFetchTime, sinceLastFecth, prevFetchTime = System.currentTimeMillis();
        for (String url : Arrays.asList(URL_USER_AGENT_1, URL_DELAY_1, URL_USER_AGENT_2, URL_USER_AGENT_1)) {
            System.out.println("Fetch with delay = "+fetcher.getDelay());
            FetchResult<CloseableHttpResponse> summary = fetcher.fetch(url);
            currFetchTime = System.currentTimeMillis();
            sinceLastFecth = currFetchTime - prevFetchTime;
            prevFetchTime = currFetchTime;
            summary.getResponse().close();

            assertThat(sinceLastFecth, greaterThanOrEqualTo(fetcher.getDelay() + summary.getResponseTime()));

            fetcher.setDelay(fetcher.getDelay()+200);
        }
    }

    @Test
    public void keepDelayAfterFailTest() throws Exception {
        final long delay = 200L;
        DelayFetcher fetcher = new DelayFetcher(httpClient, delay);
        try {
            fetcher.fetch(URL_CONNECTION_REFUSED);
        } catch (FetchException exc) {/*Ignore it*/}
        long startTime = System.currentTimeMillis();
        FetchResult<CloseableHttpResponse> summary = fetcher.fetch(URL_USER_AGENT_1);
        long elapsedTime = System.currentTimeMillis() - startTime;
        summary.getResponse().close();
        assertThat("Delay should be preserved after fetching fails",
                elapsedTime, greaterThanOrEqualTo(delay + summary.getResponseTime()));
    }

    @Test
    public void noSuchPageTest() throws Exception {
        DelayFetcher fetcher = new DelayFetcher(httpClient, 100L);
        for (String url : Arrays.asList(URL_NO_SUCH_PAGE_1, URL_NO_SUCH_PAGE_2)) {
            try(CloseableHttpResponse response = fetcher.fetch(url).getResponse()) {
                assertThat("Http response code should be 404",
                        ResponseUtil.getHttpStatusCode(response), is(HttpServletResponse.SC_NOT_FOUND));
            }
        }
    }

    @Test(expected = FetchException.class)
    public void connectionRefusedTest() throws Exception {
        new DelayFetcher(httpClient).fetch(URL_CONNECTION_REFUSED);
    }

    @Test(expected = FetchException.class)
    public void unknownHostTest() throws Exception {
        new DelayFetcher(httpClient).fetch(URL_UNKNOWN_HOST);
    }

    @Test(expected = FetchException.class)
    public void malformedUrlTest() throws Exception {
        new DelayFetcher(httpClient).fetch(URL_MALFORMED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void urlEmptyStringTest() throws Exception {
        new DelayFetcher(httpClient).fetch("");
    }

}
