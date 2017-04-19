package org.izolotov.crawler.parse.jsoup;

import com.google.common.base.Optional;
import org.izolotov.crawler.WebPage;
import org.izolotov.crawler.parse.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Collection;
import java.util.stream.Collectors;

public class JsoupDocumentBuilder implements DocumentBuilder {

    private String text;
    private Collection<String> outlinks;
    private ParseFlag flag;

    public JsoupDocumentBuilder(WebPage page) {
        try {
            Document doc = Jsoup.parse(page.getContent(), page.getUrlString());
            this.text = doc.text();
            this.outlinks = doc.select("a[href]").stream().
                    map(link -> link.attr("abs:href")).collect(Collectors.toSet());
            flag = ParseFlag.SUCCESS;
        } catch (Exception exc) {
            flag = ParseFlag.FAIL;
        }
    }

    @Override
    public DocumentBuilder setStatus(Parsable parsable) {
        flag.setStatus(parsable);
        return this;
    }

    @Override
    public DocumentBuilder setText(HasText  hasText) {
        hasText.setText(text);
        return this;
    }

    @Override
    public DocumentBuilder addOutlinks(HasOutlinks hasOutlinks) {
        hasOutlinks.getOutlinks().addAll(outlinks);
        return this;
    }
}
