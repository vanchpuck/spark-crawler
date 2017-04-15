package org.izolotov.crawler.fetch;

import org.apache.http.HttpResponse;
import org.izolotov.crawler.fetch.FetchStatus.Flag;
import org.izolotov.crawler.WebPage;

public class DummyHandler implements ResponseHandler {

    @Override
    public void handle(WebPage page, HttpResponse response) {
        System.out.println("Can't handle url " + page.getUrlString() + " with status code " + response.getStatusLine().getStatusCode());
        FailFlag.OTHER.setStatus(page, "Can't handle response with code " + response.getStatusLine().getStatusCode());
    }

}
