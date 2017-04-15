package org.izolotov.crawler;

/* SimpleApp.java */

import java.io.Serializable;
import java.util.List;

import org.apache.spark.api.java.*;
import org.apache.spark.SparkConf;

import org.izolotov.crawler.fetch.PageFetcher;
import org.izolotov.crawler.fetch.UserAgent;

// TODO robots.txt
public class SimpleApp implements Serializable {

    private JavaSparkContext sc;
    private UserAgent userAgent;
//	Broadcast<HbaseTableFormatter> formatterBrdcst;

    public SimpleApp(JavaSparkContext sparkContext) throws Exception {
        sc = sparkContext;// new JavaSparkContext(sparkConf);
        userAgent = new UserAgent("NoNameYetBot");
//		formatterBroadcast = sc.broadcast(new HbaseTableFormatter());
    }

    public static void main(String[] args) throws Exception {
        SparkConf sparkConf = new SparkConf().setAppName("Simple Application");
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        SimpleApp app = new SimpleApp(sparkContext);
        app.crawl(args[0], Integer.valueOf(args[1]));
//		fetched.flatMap(tuple -> tuple._2).
//				mapToPair(formatterBroadcast.value()::format).saveAsNewAPIHadoopDataset(hbaseClient.getConf());
    }

    public JavaRDD<WebPage> crawl(String path, int maxDepth) {
        JavaRDD<WebPage> uncrawledPages = sc.textFile(path).map(WebPage::of);
        return crawl(uncrawledPages, maxDepth);
    }

    public JavaRDD<WebPage> crawl(JavaRDD<WebPage> pages, int maxDepth) {
        final PageFetcher.Builder builder = new PageFetcher.Builder(userAgent).
                setMinDelay(sc.getConf().getLong("fetch.delay.min", 5000L)).
                setConnectionTimeLimit(sc.getConf().getLong("fetch.connection.time.limit", 5000L));

        JavaPairRDD<String, List<WebPage>> fetched = pages.groupBy(page -> page.getUrl().getHost()).
                mapValues(hostUrls -> builder.build().fetch(hostUrls));

        JavaPairRDD<String, List<WebPage>> fetchedWithRedirects = new SimpleRedirectWalker(builder, maxDepth).apply(fetched);

        JavaRDD<WebPage> result = fetchedWithRedirects.flatMap(tuple -> tuple._2).distinct();

        return result;
    }

}
