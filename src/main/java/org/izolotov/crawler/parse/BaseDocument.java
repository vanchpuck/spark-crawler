package org.izolotov.crawler.parse;

import org.izolotov.crawler.HasContent;
import org.izolotov.crawler.HasUrl;
import org.izolotov.crawler.Status;
import org.izolotov.crawler.WebPage;

public class BaseDocument implements Parsable{

    private Status parseStatus;

    public BaseDocument(Status parseStatus) {
        this.parseStatus = parseStatus;
    }

    @Override
    public Status getParseStatus() {
        return parseStatus;
    }

}
