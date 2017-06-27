package org.izolotov.crawler;

import org.izolotov.crawler.fetch.FetchFlag;
import org.izolotov.crawler.fetch.PageFetcher;
import org.izolotov.crawler.parse.ParseFlag;
import org.izolotov.crawler.parse.TextDocument;
import org.izolotov.crawler.parse.jsoup.JsoupDocumentBuilder;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CrawlQueue implements Serializable {

    private final PageFetcher fetcher;
    private final String host;
    private final int depth;

    public CrawlQueue(String host, PageFetcher.Builder builder, int depth) {
        Objects.requireNonNull(host);
        this.host = host;
        this.depth = depth;
        fetcher = builder.build();
    }

    public List<TextDocument> crawl(Iterable<WebPage> urls) {
        Map<String, TextDocument> parsedAll = new HashMap<>();
        List<WebPage> uncrawled = StreamSupport.stream(urls.spliterator(), false).collect(Collectors.toList());
//        System.out.println("Pages to crawl: " + String.join(", ", uncrawled.stream().map(WebPage::getUrlString).collect(Collectors.toList())));

        for (int i = 0; i <= depth; i++) {
            Map<String, TextDocument> parsed = fetcher.fetch(uncrawled).stream()
                    .map(page -> new TextDocument(new JsoupDocumentBuilder(page)))
                    .collect(Collectors.toMap(doc -> doc.getUrl().toString(), Function.identity()));

            parsedAll.putAll(parsed);

            List<WebPage> redirects = parsed.values().stream().filter(doc -> FetchFlag.REDIRECT.check(doc) || ParseFlag.META_REDIRECT.check(doc))
                    .map(doc -> {
                        String target = FetchFlag.REDIRECT.getStatusMessage(doc).orNull();
                        if (target != null) {
                            return target;
                        }
                        target = ParseFlag.META_REDIRECT.getStatusMessage(doc).orNull();
                        if (target != null) {
                            return target;
                        }
                        return null;
                    })
                    .filter(url -> !parsedAll.keySet().contains(url))
                    .map(WebPage::of)
                    .collect(Collectors.toList());

            Map<Boolean, List<WebPage>> groups = redirects.stream()
                    .collect(Collectors.partitioningBy(page -> page.getUrl().getHost().equals(host)));

            List<TextDocument> list = groups.get(Boolean.FALSE).stream()
                    .map(page -> new TextDocument(new JsoupDocumentBuilder(page))).collect(Collectors.toList());
            Map<String, TextDocument> remoteHostDocs = list.stream().
                    collect(Collectors.toMap(doc -> doc.getUrl().toString(), Function.identity(), (doc1, doc2) -> doc1));

            parsedAll.putAll(remoteHostDocs);
            uncrawled = groups.get(Boolean.TRUE);
        }
        return new ArrayList<>(parsedAll.values());
    }
}
