package org.izolotov.crawler.fetch;

import org.apache.http.HttpResponse;
import org.izolotov.crawler.fetch.FetchStatus.Flag;
import org.izolotov.crawler.WebPage;


// TODO maybe it should be the superclass for ContentHandler
public class NoContentHandler implements ResponseHandler {

    @Override
    public void handle(WebPage page, HttpResponse response) {
        Flag.SUCCESS.setStatus(page);
    }

}
