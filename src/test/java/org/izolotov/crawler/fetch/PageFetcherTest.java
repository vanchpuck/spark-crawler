package org.izolotov.crawler.fetch;

import static org.hamcrest.Matchers.*;
import static org.izolotov.crawler.fetch.PageFetcher.RESPONSE_TIME;
import static org.junit.Assert.*;

import org.apache.commons.httpclient.HttpStatus;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.*;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.izolotov.crawler.WebPage;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class PageFetcherTest {

    public static final int PORT = 8081;
    public static final String CONTENT_TYPE = "text/html;charset=utf-8";
    //    public static final String CONTENT = "<h1>Hello world!</h1>";
    public static final String USER_AGENT_NAME = "TestAgent";

    public enum Page {
        PAGE_1(String.format("http://localhost:%d/success_1.html", PORT)),
        PAGE_2(String.format("http://localhost:%d/success_2.html", PORT)),
        PAGE_3(String.format("http://localhost:%d/success_3.html", PORT)),
        PAGE_4(String.format("http://localhost:%d/success_4.html", PORT)),
        UNKNOWN_HOST_PAGE_1(String.format("http://localhost_unknown:%d/unknown_host_1.html", PORT)),
        UNKNOWN_HOST_PAGE_2(String.format("http://localhost_unknown:%d/unknown_host_2.html", PORT));

        private final String url;

        Page(String url) {
            this.url = url;
        }

        @Override
        public String toString() {
            return url;
        }
    }

    public static class ResponseHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            try {
                Thread.sleep(50L);
                response.setContentType(CONTENT_TYPE);
                response.setStatus(HttpServletResponse.SC_OK);
                // send back the request User-Agent
                response.getWriter().print(baseRequest.getHeader(HttpHeader.USER_AGENT.toString()));
                baseRequest.setHandled(true);
            } catch (InterruptedException e) {
                baseRequest.setHandled(false);
            }
        }
    }

    private static Server server;
    private static UserAgent userAgent;

    @BeforeClass
    public static void setUp() throws Exception {
        userAgent = new UserAgent(USER_AGENT_NAME);
        server = new Server(PORT);
        ResponseHandler servletHandler = new ResponseHandler();
        server.setHandler(servletHandler);
        server.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void fetcherTest() {
        PageFetcher fetcher = new PageFetcher.Builder(userAgent).setMinDelay(50L).build();

        List<String> urls = new ArrayList<>();
        urls.add(Page.PAGE_1.toString());
        urls.add(Page.PAGE_2.toString());
        urls.add(Page.PAGE_3.toString());

        List<WebPage> fetchExpected = urls.stream().map(PageFetcherTest::newCrawledPage).collect(Collectors.toList());
        List<WebPage> fetchedActual = fetcher.fetch(urls.stream().map(WebPage::of).collect(Collectors.toList()));

        assertThat(fetchedActual, containsInAnyOrder(fetchExpected.toArray()));
    }

    @Test
    public void fetcherMinDelayTest() {
        PageFetcher fetcher = new PageFetcher.Builder(userAgent).setMinDelay(100L).build();

        List<String> urls = new ArrayList<>();
        urls.add(Page.PAGE_1.toString());
        urls.add(Page.PAGE_2.toString());
        urls.add(Page.PAGE_3.toString());
        urls.add(Page.PAGE_4.toString());

        long startTime = System.currentTimeMillis();
        fetcher.fetch(urls.stream().map(WebPage::of).collect(Collectors.toList()));
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        assertThat(elapsedTime, greaterThan((urls.size() - 1) * fetcher.getMinDelay()));
    }

    @Test
    public void fetchTimeLimitTest() {
        PageFetcher fetcher = new PageFetcher.Builder(userAgent).setMinDelay(1100L).build();

        List<String> urls = new ArrayList<>();
        urls.add(Page.PAGE_1.toString());
        urls.add(Page.PAGE_2.toString());
        urls.add(Page.PAGE_3.toString());

        List<WebPage> fetched = fetcher.fetch(urls.stream().map(WebPage::of).collect(Collectors.toList()), 1000L);

        assertThat(fetched.stream().filter(FetchStatus.Flag.SUCCESS::check).collect(Collectors.toList()).size(), is(1));
        assertThat(fetched.stream().filter(FetchStatus.Flag.NOT_FETCHED_YET::check).collect(Collectors.toList()).size(), is(2));
    }

    @Test
    public void unknownHostTest() {
        PageFetcher fetcher = new PageFetcher.Builder(userAgent).setMinDelay(100L).build();

        List<String> urls = new ArrayList<>();
        urls.add(Page.UNKNOWN_HOST_PAGE_1.toString());
        urls.add(Page.UNKNOWN_HOST_PAGE_2.toString());

        List<WebPage> fetchedActual = fetcher.fetch(urls.stream().map(WebPage::of).collect(Collectors.toList()));
        List<WebPage> fetchedExpected = urls.stream().map(page -> newFailedPage(page, FailFlag.CONNECTION_ISSUE)).collect(Collectors.toList());

        assertThat(fetchedActual, containsInAnyOrder(fetchedExpected.toArray()));
    }

    @Test
    public void responseTimeTest() {
        PageFetcher fetcher = new PageFetcher.Builder(userAgent).setMinDelay(100L).build();

        List<String> urls = new ArrayList<>();
        urls.add(Page.PAGE_1.toString());

        long startTime = System.currentTimeMillis();
        List<WebPage> fetched = fetcher.fetch(urls.stream().map(WebPage::of).collect(Collectors.toList()));
        long elapsedTime = System.currentTimeMillis() - startTime;

        assertThat(Long.valueOf(fetched.get(0).getFetchStatus().getInfo().get(RESPONSE_TIME)),
                allOf(lessThan(elapsedTime), greaterThan(0L)));
    }

    @Test
    public void connectionTimeLimitTest() {
        PageFetcher fetcher = new PageFetcher.Builder(userAgent).setMinDelay(50L).setConnectionTimeLimit(20L).build();

        List<String> urls = new ArrayList<>();
        urls.add(Page.PAGE_1.toString());
        urls.add(Page.PAGE_2.toString());

        List<WebPage> fetchedActual = fetcher.fetch(urls.stream().map(WebPage::of).collect(Collectors.toList()));
        List<WebPage> fetchedExpected = urls.stream().map(page -> newFailedPage(page, FailFlag.TIMEOUT)).collect(Collectors.toList());

        assertThat(fetchedActual, containsInAnyOrder(fetchedExpected.toArray()));
    }

    private static WebPage newCrawledPage(String url) {
        WebPage page = WebPage.of(url);
        page.setContentType(CONTENT_TYPE);
        page.setContent(USER_AGENT_NAME);
        page.setHttpStatusCode(HttpStatus.SC_OK);
        FetchStatus.Flag.SUCCESS.setStatus(page);
        return page;
    }

    private static WebPage newFailedPage(String url, FailFlag flag) {
        WebPage page = WebPage.of(url);
        FailFlag.CONNECTION_ISSUE.setStatus(page);
        return page;
    }

}
