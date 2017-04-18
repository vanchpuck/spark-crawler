package org.izolotov.crawler.parse.jsoup;

import com.google.common.base.Optional;
import org.izolotov.crawler.HasContent;
import org.izolotov.crawler.Status;
import org.izolotov.crawler.WebPage;
import org.izolotov.crawler.parse.BaseDocument;
import org.izolotov.crawler.parse.HasOutlinks;
import org.izolotov.crawler.parse.HasText;
import org.izolotov.crawler.parse.ParseFlag;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

// TODO Sanitize untrusted HTML (to prevent XSS)
// TODO hashCode & equals
public class TextDocument extends BaseDocument implements HasText, HasOutlinks{

    private String text;
    private Collection<String> outlinks;
    private Status status;

    public TextDocument(WebPage page) {
        super(page);
        try {
            parse();
            status = new Status(ParseFlag.SUCCESS);
        } catch (Exception exc) {
            status = new Status(ParseFlag.FAIL);
        }
    }

    @Override
    public Status getParseStatus() {
        return status;
    }

    @Override
    public Collection<String> getOutlinks() {
        return new ArrayList<>(outlinks);
    }

    @Override
    public Optional<String> getText() {
        return Optional.fromNullable(text);
    }

    private void parse() throws Exception {
        Document doc = Jsoup.parse(super.getContent(), super.getUrlString());
        this.text = doc.text();
        this.outlinks = doc.select("a[href]").stream().
                map(link -> link.attr("abs:href")).collect(Collectors.toSet());
    }

}
