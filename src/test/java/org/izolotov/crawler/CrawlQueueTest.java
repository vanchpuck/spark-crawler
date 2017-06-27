package org.izolotov.crawler;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.izolotov.crawler.BaseCrawlTest.*;
import static org.junit.Assert.*;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.eclipse.jetty.server.Server;
import org.izolotov.crawler.fetch.FetchFlag;
import org.izolotov.crawler.fetch.PageFetcher;
import org.izolotov.crawler.fetch.UserAgent;
import org.izolotov.crawler.parse.ParseFlag;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CrawlQueueTest {

    private CrawlQueue queue;

    @BeforeClass
    public static void startServer() throws Exception {
        BaseCrawlTest.startServer();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        BaseCrawlTest.stopServer();
    }

    @Before
    public void setUp() {
        queue = new CrawlQueue("localhost",
                new PageFetcher.Builder(new UserAgent("NoNameYetBot"))
                        .setMinDelay(100L)
                        .setConnectionTimeLimit(500L),
                1);
    }

    @Test
    public void testDeepCrawl() {
        List<WebPage> input = Lists.newArrayList(
                SUCCESS_1,
                SUCCESS_2,
                REDIRECT_TO_SUCCESS_2,
                REDIRECT_TO_REDIRECT_TO_SUCCESS_3,
                REDIRECT_TO_SUCCESS_REMOTE_1,
                REDIRECT_2_TO_SUCCESS_REMOTE_1
        ).stream().map(WebPage::of).collect(Collectors.toList());

        ArrayList expected = Lists.newArrayList(
                newTriple(SUCCESS_1, ParseFlag.SUCCESS, FetchFlag.SUCCESS),
                newTriple(SUCCESS_2, ParseFlag.SUCCESS, FetchFlag.SUCCESS),
                newTriple(REDIRECT_TO_SUCCESS_2, ParseFlag.SUCCESS, FetchFlag.REDIRECT),
                newTriple(REDIRECT_TO_REDIRECT_TO_SUCCESS_3, ParseFlag.SUCCESS, FetchFlag.REDIRECT),
                newTriple(REDIRECT_TO_SUCCESS_3, ParseFlag.SUCCESS, FetchFlag.REDIRECT),
                newTriple(REDIRECT_TO_SUCCESS_REMOTE_1, ParseFlag.SUCCESS, FetchFlag.REDIRECT),
                newTriple(REDIRECT_2_TO_SUCCESS_REMOTE_1, ParseFlag.SUCCESS, FetchFlag.REDIRECT),
                newTriple(SUCCESS_REMOTE_1, ParseFlag.SUCCESS, FetchFlag.NOT_FETCHED_YET)
        );

        List<ImmutableTriple<String, String, String>> actual = queue.crawl(input).stream()
                .map(doc -> new ImmutableTriple<>(
                        doc.getUrl().toString(),
                        doc.getParseStatus().getFlag().toString(),
                        doc.getFetchStatus().getFlag().toString()))
                .collect(Collectors.toList());

        assertThat(actual, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void testNoData() {
        assertThat(queue.crawl(Collections.emptyList()).size(), is(0));
    }

    private ImmutableTriple<String, String, String> newTriple(String url, ParseFlag parseFlag, FetchFlag fetchFlag) {
        return new ImmutableTriple<>(url, parseFlag.toString(), fetchFlag.toString());
    }

}
