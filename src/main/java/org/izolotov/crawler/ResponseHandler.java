package org.izolotov.crawler;

import java.util.function.BiConsumer;

import org.apache.http.HttpResponse;

public interface ResponseHandler extends BiConsumer<WebPage, HttpResponse> {

//	public WebPage getWebPage(String url, HttpResponse response);
	
}
