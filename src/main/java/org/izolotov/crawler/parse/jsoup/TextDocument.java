package org.izolotov.crawler.parse.jsoup;

import com.google.common.base.Optional;
import org.izolotov.crawler.HasContent;
import org.izolotov.crawler.WebPage;
import org.izolotov.crawler.parse.HasOutlinks;
import org.izolotov.crawler.parse.HasText;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

// TODO Sanitize untrusted HTML (to prevent XSS)
public class TextDocument implements HasText, HasOutlinks, HasContent{

    private WebPage page;
    private String text;
    private Collection<String> outlinks;

    public TextDocument(WebPage page) {
        this.page = page;
        parse();
    }

    @Override
    public Collection<String> getOutlinks() {
        return new ArrayList<>(outlinks);
    }

    @Override
    public Optional<String> getText() {
        return Optional.fromNullable(text);
    }

    private void parse() {
        Document doc = Jsoup.parse(page.getContent(), page.getUrlString());
        this.text = doc.text();
        this.outlinks = doc.select("a[href]").stream().
                map(link -> link.attr("abs:href")).collect(Collectors.toSet());
    }

    @Override
    public String getContentType() {
        return page.getContentType();
    }

    @Override
    public String getContent() {
        return page.getContent();
    }
}
