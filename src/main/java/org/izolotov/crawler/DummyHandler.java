package org.izolotov.crawler;

import org.apache.http.HttpResponse;
import org.izolotov.crawler.FetchStatus.Flag;

public class DummyHandler implements ResponseHandler {

	@Override
	public void handle(WebPage page, HttpResponse response) {
		System.out.println("Can't handle url "+page.getUrl()+" with status code "+response.getStatusLine().getStatusCode());
		Flag.FAIL.setStatus(page, "Can't handle response with code " + response.getStatusLine().getStatusCode());
	}

}
