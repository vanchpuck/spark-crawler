package org.izolotov.crawler.parse;

import org.izolotov.crawler.HasContent;
import org.izolotov.crawler.HasUrl;
import org.izolotov.crawler.Status;
import org.izolotov.crawler.WebPage;

public abstract class BaseDocument implements Parsable, HasContent, HasUrl{

    private WebPage page;

    public BaseDocument(WebPage page) {
        this.page = page;
    }

    @Override
    public abstract Status getParseStatus();

    @Override
    public String getContentType() {
        return page.getContentType();
    }

    @Override
    public String getContent() {
        return page.getContent();
    }

    @Override
    public String getUrlString() {
        return page.getUrlString();
    }
}
