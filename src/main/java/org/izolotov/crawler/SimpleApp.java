package org.izolotov.crawler;

/* SimpleApp.java */

import java.io.Serializable;
import java.util.*;
import org.apache.spark.api.java.*;
import org.apache.spark.SparkConf;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.storage.StorageLevel;
import org.izolotov.crawler.fetch.FetchFlag;
import org.izolotov.crawler.fetch.PageFetcher;
import org.izolotov.crawler.fetch.UserAgent;
import org.izolotov.crawler.parse.TextDocument;
import org.izolotov.crawler.parse.jsoup.JsoupDocumentBuilder;

// TODO robots.txt
public class SimpleApp implements Serializable {

    private JavaSparkContext sc;
    private UserAgent userAgent;
//    private Broadcast<HbaseTableFormatter> formatterBrdcst;
//    private HbaseClient hbaseClient;

    public SimpleApp(JavaSparkContext sparkContext) throws Exception {
        sc = sparkContext;// new JavaSparkContext(sparkConf);
        userAgent = new UserAgent("NoNameYetBot");
//        formatterBrdcst = sc.broadcast(new HbaseTableFormatter());
//        hbaseClient = new HbaseClient("spark_crawler_test");
    }

    public static void main(String[] args) throws Exception {
        SparkConf sparkConf = new SparkConf().setAppName("Simple Application");
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        SimpleApp app = new SimpleApp(sparkContext);
        JavaRDD<TextDocument> fetched = app.crawl(args[0], Integer.valueOf(args[1]));
        fetched.foreach(doc -> System.out.println(doc.getUrl()+" "+doc.getFetchStatus().toString()));

//		fetched.flatMap(tuple -> tuple._2).
//				mapToPair(formatterBrdcst.value()::format).saveAsNewAPIHadoopDataset(hbaseClient.getConf());
    }

    public JavaRDD<TextDocument> crawl(String path, int maxRedirectDepth) {
        JavaRDD<WebPage> uncrawledPages = sc.textFile(path).map(WebPage::of);
        return crawl(uncrawledPages, maxRedirectDepth);
    }

    //TODO track the redirect depth in status info
    public JavaRDD<TextDocument> crawl(JavaRDD<WebPage> pages, int maxRedirectDepth) {
        final PageFetcher.Builder builder = new PageFetcher.Builder(userAgent).
                setMinDelay(sc.getConf().getLong("fetch.delay.min", 2000L)).
                setConnectionTimeLimit(sc.getConf().getLong("fetch.connection.time.limit", 5000L));

        //TODO Try to avoid persisting. Maybe we should shuffle all processed docs (not only uncrawled)
        JavaRDD<TextDocument> processed = crawl(pages, builder, maxRedirectDepth).persist(StorageLevel.MEMORY_AND_DISK());
        JavaRDD<WebPage> uncrawled = getUncrawled(processed);
        JavaRDD<TextDocument> crawledAll = getСrawled(processed);
        for (int i = 0; i < maxRedirectDepth; i++) {
            processed  = crawl(uncrawled, builder, maxRedirectDepth).persist(StorageLevel.MEMORY_AND_DISK());
            crawledAll = crawledAll.union(getСrawled(processed));
            uncrawled = getUncrawled(processed);
        }

        return crawledAll;
    }

    //TODO Use .mapPartitions instead of .mapValues
    private JavaRDD<TextDocument> crawl(JavaRDD<WebPage> pages, PageFetcher.Builder builder, int maxRedirectDepth) {
        JavaRDD<TextDocument> processed = pages.groupBy(page -> page.getUrl().getHost())
                .mapValues(hostUrls -> new CrawlQueue(hostUrls.iterator().next().getUrl().getHost(), builder, maxRedirectDepth).crawl(hostUrls))
                .flatMap(tuple -> tuple._2);
        return processed;
    }

    private JavaRDD<WebPage> getUncrawled(JavaRDD<TextDocument> documents) {
        return documents.filter(doc -> FetchFlag.NOT_FETCHED_YET.check(doc))
                .map(doc -> new WebPage(doc.getUrl()));
    }

    private JavaRDD<TextDocument> getСrawled(JavaRDD<TextDocument> documents) {
        return documents.filter(doc -> !FetchFlag.NOT_FETCHED_YET.check(doc));
    }

}
