package org.izolotov.crawler;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.izolotov.crawler.fetch.FetchFlag;
import org.izolotov.crawler.fetch.PageFetcher;

public class SimpleRedirectWalker implements Serializable  /*Function<JavaPairRDD<String, List<WebPage>>, JavaPairRDD<String, List<WebPage>>>,*/ {

    private final PageFetcher.Builder fetcherBuilder;
    private final int maxDeep;

    public SimpleRedirectWalker(PageFetcher.Builder fetcherBuilder, int maxDeep) {
        this.fetcherBuilder = fetcherBuilder;
        this.maxDeep = maxDeep;
    }

    public JavaPairRDD<String, List<WebPage>> apply(JavaPairRDD<String, List<WebPage>> fetchedPages) {
        return hop(fetchedPages, 0);
    }

    private JavaPairRDD<String, List<WebPage>> hop(JavaPairRDD<String, List<WebPage>> fetchedPages, int deep) {
        if (deep >= maxDeep) {
            return fetchedPages;
        }
        JavaRDD<String> redirectUrls =
                fetchedPages.flatMap(hostPages -> hostPages._2.stream().
                        filter(FetchFlag.REDIRECT::check).
                        map(page -> FetchFlag.REDIRECT.getStatusMessage(page).or("null")).
                        distinct().collect(Collectors.toList()));

        JavaPairRDD<String, List<WebPage>> fetchedRedirects =
                redirectUrls.map(WebPage::of).
                        groupBy(page -> page.getUrl().getHost()).
                        mapValues(hostUrls -> fetcherBuilder.build().fetch(hostUrls));
        return fetchedPages.union(hop(fetchedRedirects, deep + 1));
    }
}
