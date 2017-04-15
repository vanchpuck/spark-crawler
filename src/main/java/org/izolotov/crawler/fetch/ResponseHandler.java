package org.izolotov.crawler.fetch;

import org.apache.http.HttpResponse;
import org.izolotov.crawler.WebPage;

import java.io.Serializable;

public interface ResponseHandler extends Serializable {

    public void handle(WebPage page, HttpResponse response);

}
