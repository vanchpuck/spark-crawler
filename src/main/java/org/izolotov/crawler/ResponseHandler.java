package org.izolotov.crawler;

import org.apache.http.HttpResponse;

public interface ResponseHandler {

	public void handle(WebPage page, HttpResponse response);
	
}
