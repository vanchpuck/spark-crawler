package org.izolotov.crawler;

import static org.izolotov.crawler.BaseCrawlTest.*;

import com.google.common.collect.Lists;
import com.holdenkarau.spark.testing.JavaRDDComparisons;
import com.holdenkarau.spark.testing.SharedJavaSparkContext;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.izolotov.crawler.fetch.FetchFlag;
import org.izolotov.crawler.parse.TextDocument;
import org.izolotov.crawler.parse.jsoup.JsoupDocumentBuilder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class SimpleAppTest extends SharedJavaSparkContext {

    private SimpleApp app;

    public TextDocument newExpectedSuccess(String url) throws Exception {
        WebPage page = WebPage.of(url);
        page.setContent(SUCCESS_CONTENT);
        page.setContentType(CONTENT_TYPE);
        page.setHttpStatusCode(HttpStatus.SC_OK);
        FetchFlag.SUCCESS.setStatus(page);
        return new TextDocument(new JsoupDocumentBuilder(page));
    }

    public TextDocument newExpectedRedirect(String url, String targetUrl) throws Exception {
        WebPage page = WebPage.of(url);
        page.setHttpStatusCode(HttpStatus.SC_MOVED_TEMPORARILY);
        FetchFlag.REDIRECT.setStatus(page, targetUrl);
        return new TextDocument(new JsoupDocumentBuilder(page));
    }

    public TextDocument newExpectedFail(String url) throws Exception {
        WebPage page = WebPage.of(url);
        FetchFlag.FAIL.setStatus(page);
        return new TextDocument(new JsoupDocumentBuilder(page));
    }

    @BeforeClass
    public static void startServer() throws Exception {
        BaseCrawlTest.startServer();
    }

    @Before
    public void runBefore()  {
        super.runBefore();
        try {
            jsc().getConf().set("fetch.delay.min", "100");
            jsc().getConf().set("fetch.connection.time.limit", "2000");
            app = new SimpleApp(jsc());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public SparkConf conf() {
        return super.conf().
                set("fetch.delay.min", "100").
                set("fetch.connection.time.limit", "2000");
    }

    @Test
    public void multipleHostsTestTest() throws Exception {
        List<WebPage> input = Lists.newArrayList(
                SUCCESS_1,
                REDIRECT_TO_SUCCESS_2,
                REDIRECT_TO_SUCCESS_REMOTE_1
        ).stream().map(WebPage::of).collect(Collectors.toList());

        List<TextDocument> expectedOutput = Lists.newArrayList(
                newExpectedSuccess(SUCCESS_1),
                newExpectedSuccess(SUCCESS_2),
                newExpectedRedirect(REDIRECT_TO_SUCCESS_2, SUCCESS_2),
                newExpectedRedirect(REDIRECT_TO_SUCCESS_REMOTE_1, SUCCESS_REMOTE_1),
                newExpectedFail(SUCCESS_REMOTE_1)
        );

        JavaRDD<WebPage> uncrawledRdd = jsc().parallelize(input);
        JavaRDD<TextDocument> expectedRdd = jsc().parallelize(expectedOutput);

        SimpleApp app = new SimpleApp(jsc());

        JavaRDD<TextDocument> actualRdd = app.crawl(uncrawledRdd, 2);
        JavaRDDComparisons.assertRDDEquals(expectedRdd, actualRdd);
    }

    @Test
    public void noHopRedirectsCrawlTest() throws Exception {
        List<WebPage> input = Lists.newArrayList(
                SUCCESS_1,
                SUCCESS_2,
                REDIRECT_TO_SUCCESS_2,
                REDIRECT_TO_REDIRECT_TO_SUCCESS_3
        ).stream().map(WebPage::of).collect(Collectors.toList());

        List<TextDocument> expectedOutput = Lists.newArrayList(
                newExpectedSuccess(SUCCESS_1),
                newExpectedSuccess(SUCCESS_2),
                newExpectedRedirect(REDIRECT_TO_SUCCESS_2, SUCCESS_2),
                newExpectedRedirect(REDIRECT_TO_REDIRECT_TO_SUCCESS_3, REDIRECT_TO_SUCCESS_3)
        );

        JavaRDD<WebPage> uncrawledRdd = jsc().parallelize(input);
        JavaRDD<TextDocument> expectedRdd = jsc().parallelize(expectedOutput);

        SimpleApp app1 = new SimpleApp(jsc());

        JavaRDD<TextDocument> actualRdd = app1.crawl(uncrawledRdd, 0);
        JavaRDDComparisons.assertRDDEquals(expectedRdd, actualRdd);
    }

    @Test
    public void followRedirectsCrawlTest() throws Exception {
        List<WebPage> input = Lists.newArrayList(
                SUCCESS_1,
                REDIRECT_TO_SUCCESS_2,
                REDIRECT_TO_REDIRECT_TO_SUCCESS_3
        ).stream().map(WebPage::of).collect(Collectors.toList());

        List<TextDocument> expectedOutput = Lists.newArrayList(
                // SUCCESS_1 chain
                newExpectedSuccess(SUCCESS_1),
                // REDIRECT_TO_SUCCESS_2 chain
                newExpectedRedirect(REDIRECT_TO_SUCCESS_2, SUCCESS_2),
                newExpectedSuccess(SUCCESS_2),
                // REDIRECT_TO_REDIRECT_TO_SUCCESS_3 chain
                newExpectedRedirect(REDIRECT_TO_REDIRECT_TO_SUCCESS_3, REDIRECT_TO_SUCCESS_3),
                newExpectedRedirect(REDIRECT_TO_SUCCESS_3, SUCCESS_3),
                newExpectedSuccess(SUCCESS_3)
        );

        JavaRDD<WebPage> uncrawledRdd = jsc().parallelize(input);
        JavaRDD<TextDocument> expectedRdd = jsc().parallelize(expectedOutput);

        SimpleApp app = new SimpleApp(jsc());

        JavaRDD<TextDocument> actualRdd = app.crawl(uncrawledRdd, 2);
        JavaRDDComparisons.assertRDDEquals(expectedRdd, actualRdd);
    }

    @Test
    public void followRedirectsRecrawlTest() throws Exception {
        List<WebPage> input = Lists.newArrayList(
                SUCCESS_1,
                SUCCESS_2,
                REDIRECT_TO_SUCCESS_2
        ).stream().map(WebPage::of).collect(Collectors.toList());

        // Output should't contain page duplicates even when redirect target page has been crawled earlier
        List<TextDocument> expectedOutput = Lists.newArrayList(
                newExpectedSuccess(SUCCESS_1),
                newExpectedSuccess(SUCCESS_2),
                newExpectedRedirect(REDIRECT_TO_SUCCESS_2, SUCCESS_2)
        );

        JavaRDD<WebPage> uncrawledRdd = jsc().parallelize(input);
        JavaRDD<TextDocument> expectedRdd = jsc().parallelize(expectedOutput);

        SimpleApp app = new SimpleApp(jsc());

        JavaRDD<TextDocument> actualRdd = app.crawl(uncrawledRdd, 1);
        JavaRDDComparisons.assertRDDEquals(expectedRdd, actualRdd);
    }

    @Test
    public void followRedirectsParseTest() throws Exception {
        List<WebPage> input = Lists.newArrayList(
                SUCCESS_1,
                SUCCESS_2,
                REDIRECT_TO_SUCCESS_2,
                REDIRECT_TO_REDIRECT_TO_SUCCESS_3
        ).stream().map(WebPage::of).collect(Collectors.toList());

        // Output should't contain page duplicates even when redirect target page has been crawled earlier
        List<TextDocument> expectedOutput = Lists.newArrayList(
                newExpectedSuccess(SUCCESS_1),
                newExpectedSuccess(SUCCESS_2),
                newExpectedRedirect(REDIRECT_TO_SUCCESS_2, SUCCESS_2),
                newExpectedRedirect(REDIRECT_TO_REDIRECT_TO_SUCCESS_3, REDIRECT_TO_SUCCESS_3),
                newExpectedRedirect(REDIRECT_TO_SUCCESS_3, SUCCESS_3),
                newExpectedSuccess(SUCCESS_3)
        );

        JavaRDD<WebPage> uncrawledRdd = jsc().parallelize(input);
        JavaRDD<TextDocument> expectedRdd = jsc().parallelize(expectedOutput);

        SimpleApp app = new SimpleApp(jsc());

        JavaRDD<TextDocument> actualRdd = app.crawl(uncrawledRdd,2);

        JavaRDDComparisons.assertRDDEquals(expectedRdd, actualRdd);
        expectedOutput.forEach(System.out::println);
    }

    @AfterClass
    public static void stopServer() throws Exception {
        BaseCrawlTest.stopServer();
    }
}
