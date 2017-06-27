package org.izolotov.crawler.parse.jsoup;

import org.izolotov.crawler.Fetchable;
import org.izolotov.crawler.HasUrl;
import org.izolotov.crawler.Status;
import org.izolotov.crawler.WebPage;
import org.izolotov.crawler.parse.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.Serializable;
import java.net.URL;
import java.util.Collection;
import java.util.stream.Collectors;

// TODO Sanitize untrusted HTML (to prevent XSS)
public class JsoupDocumentBuilder implements DocumentBuilder, Serializable {

    private final static String REFRESH = "refresh";
    private final static String CONTENT = "content";
    private final static String HTTP_EQUIV = "http-equiv";

    private URL url;
    private String text;
    private Collection<String> outlinks;
    private Status parseStatus;
    private Status fetchStatus;

    public JsoupDocumentBuilder(WebPage page) {
        try {
            Document doc = Jsoup.parse(page.getContent(), page.getUrlString());
            this.url = page.getUrl();
            this.fetchStatus = page.getFetchStatus();
            this.text = doc.text();
            this.outlinks = doc.select("a[href]").stream().
                    map(link -> link.attr("abs:href")).collect(Collectors.toSet());
            setFetchStatus(page);
            checkMetaRedirect(doc, page.getUrl());
            if (parseStatus == null) {
                setParseStatus(ParseFlag.SUCCESS);
            }
        } catch (Exception exc) {
            setParseStatus(ParseFlag.FAIL, exc.toString());
        }
    }

    @Override
    public DocumentBuilder setParseStatus(Parsable parsable) {
//        parseFlag.setStatus(parsable, parseStatusMsg);
        parsable.setParseStatus(parseStatus);
        return this;
    }

    @Override
    public DocumentBuilder setFetchStatus(Fetchable fetchable) {
        fetchable.setFetchStatus(fetchStatus);
        return this;
    }

    @Override
    public DocumentBuilder setText(HasText  hasText) {
        hasText.setText(text);
        return this;
    }

    @Override
    public DocumentBuilder setOutlinks(HasOutlinks hasOutlinks) {
        hasOutlinks.setOutlinks(outlinks);
        return this;
    }

    @Override
    public DocumentBuilder setUrl(HasUrl hasUrl) {
        hasUrl.setUrl(url);
        return this;
    }

    private void checkMetaRedirect(Document doc, URL baseUrl) throws Exception {
        Element element = doc.select("meta[http-equiv]").first();
        if (element != null && REFRESH.equals(element.attr(HTTP_EQUIV))) {
            String[] metaContent = element.attr(CONTENT).split("\\s*;\\s*");
            if (metaContent.length > 1) {
                String location = metaContent[1];
                setParseStatus(ParseFlag.META_REDIRECT, location);
            }
        }
    }

    private void setParseStatus(ParseFlag flag) {
        setParseStatus(flag, null);
    }

    private void setParseStatus(ParseFlag flag, String msg) {
        parseStatus = new Status(flag);
        if (msg != null) {
            flag.setMessage(parseStatus, msg);
        }
    }

}
