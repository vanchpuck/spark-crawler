package org.izolotov.crawler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.holdenkarau.spark.testing.JavaRDDComparisons;
import com.holdenkarau.spark.testing.SharedJavaSparkContext;
import javafx.scene.effect.SepiaTone;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.izolotov.crawler.fetch.FetchStatus;
import org.izolotov.crawler.fetch.PageFetcherTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.Tuple2;
import scala.reflect.ClassTag;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by izolotov on 07.04.17.
 */
public class SimpleAppTest extends SharedJavaSparkContext implements Serializable {

    public static final int PORT = 8081;
    public static Server server;

    public static final String SUCCESS_CONTENT = "<h1>Hello world!</h1>";
    public static final String CONTENT_TYPE = "text/html;charset=utf-8";

    public static final String SUCCESS_1 = String.format("http://localhost:%d/success_1.html", PORT);
    public static final String SUCCESS_2 = String.format("http://localhost:%d/success_2.html", PORT);
    public static final String SUCCESS_3 = String.format("http://localhost:%d/success_3.html", PORT);
    public static final String REDIRECT_TO_SUCCESS_2 = String.format("http://localhost:%d/redirect_to_success_2.html", PORT);
    public static final String REDIRECT_TO_SUCCESS_3 = String.format("http://localhost:%d/redirect_to_success_3.html", PORT);
    public static final String REDIRECT_TO_REDIRECT_TO_SUCCESS_3 = String.format("http://localhost:%d/redirect_to_redirect.html", PORT);

    public static class ResponseHandler extends AbstractHandler {
        public static final String SUCCESS = "sf";
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            final String requestUrl = baseRequest.getRequestURL().toString();
            if (SUCCESS_1.equals(requestUrl) ||
                    SUCCESS_2.equals(requestUrl) ||
                    SUCCESS_3.equals(requestUrl)) {
                successResponse(baseRequest, response);
            } else if (REDIRECT_TO_SUCCESS_2.equals(requestUrl)) {
                redirectResponse(SUCCESS_2, baseRequest, response);
            } else if (REDIRECT_TO_SUCCESS_3.equals(requestUrl)) {
                redirectResponse(SUCCESS_3, baseRequest, response);
            } else if (REDIRECT_TO_REDIRECT_TO_SUCCESS_3.equals(requestUrl)) {
                redirectResponse(REDIRECT_TO_SUCCESS_3, baseRequest, response);
            }
        }

        private void successResponse(Request baseRequest, HttpServletResponse response) throws IOException {
            response.setContentType(CONTENT_TYPE);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().print(SUCCESS_CONTENT);
            baseRequest.setHandled(true);
        }

        private void redirectResponse(String targetUrl, Request baseRequest, HttpServletResponse response) throws IOException {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader("location", targetUrl);
            baseRequest.setHandled(true);
        }
    }

    private SimpleApp app;

    public WebPage newExpectedSuccess(String url) throws Exception {
        WebPage page = WebPage.of(url);
        page.setContent(SUCCESS_CONTENT);
        page.setContentType(CONTENT_TYPE);
        page.setHttpStatusCode(HttpStatus.SC_OK);
        FetchStatus.Flag.SUCCESS.setStatus(page);
        return page;
    }

    public WebPage newExpectedRedirect(String url, String targetUrl) throws Exception {
        WebPage page = WebPage.of(url);
        page.setHttpStatusCode(HttpStatus.SC_MOVED_TEMPORARILY);
        FetchStatus.Flag.REDIRECT.setStatus(page, targetUrl);
        return page;
    }

    @BeforeClass
    public static void startServer() throws Exception {
        server = new Server(PORT);
        server.setHandler(new ResponseHandler());
        server.start();
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
    public void noHopRedirectsCrawlTest() throws Exception {
        List<WebPage> input = Lists.newArrayList(
                SUCCESS_1,
                SUCCESS_2,
                REDIRECT_TO_SUCCESS_2,
                REDIRECT_TO_REDIRECT_TO_SUCCESS_3
        ).stream().map(WebPage::of).collect(Collectors.toList());

        List<WebPage> expectedOutput = Lists.newArrayList(
                newExpectedSuccess(SUCCESS_1),
                newExpectedSuccess(SUCCESS_2),
                newExpectedRedirect(REDIRECT_TO_SUCCESS_2, SUCCESS_2),
                newExpectedRedirect(REDIRECT_TO_REDIRECT_TO_SUCCESS_3, REDIRECT_TO_SUCCESS_3)
        );

        JavaRDD<WebPage> uncrawledRdd = jsc().parallelize(input);
        JavaRDD<WebPage> expectedRdd = jsc().parallelize(expectedOutput);

        SimpleApp app1 = new SimpleApp(jsc());

        JavaRDD<WebPage> actualRdd = app1.crawl(uncrawledRdd, 0);

        JavaRDDComparisons.assertRDDEquals(expectedRdd, actualRdd);

    }

    @Test
    public void followRedirectsCrawlTest() throws Exception {
        List<WebPage> input = Lists.newArrayList(
                SUCCESS_1,
                REDIRECT_TO_SUCCESS_2,
                REDIRECT_TO_REDIRECT_TO_SUCCESS_3
        ).stream().map(WebPage::of).collect(Collectors.toList());

        List<WebPage> expectedOutput = Lists.newArrayList(
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
        JavaRDD<WebPage> expectedRdd = jsc().parallelize(expectedOutput);

        SimpleApp app = new SimpleApp(jsc());

        JavaRDD<WebPage> actualRdd = app.crawl(uncrawledRdd, 2);

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
        List<WebPage> expectedOutput = Lists.newArrayList(
                newExpectedSuccess(SUCCESS_1),
                newExpectedSuccess(SUCCESS_2),
                newExpectedRedirect(REDIRECT_TO_SUCCESS_2, SUCCESS_2)
        );

        JavaRDD<WebPage> uncrawledRdd = jsc().parallelize(input);
        JavaRDD<WebPage> expectedRdd = jsc().parallelize(expectedOutput);

        SimpleApp app = new SimpleApp(jsc());

        JavaRDD<WebPage> actualRdd = app.crawl(uncrawledRdd, 1);

        JavaRDDComparisons.assertRDDEquals(expectedRdd, actualRdd);
    }

    @AfterClass
    public static void stopServer() throws Exception {
        server.stop();
        runAfterClass();
    }
}
