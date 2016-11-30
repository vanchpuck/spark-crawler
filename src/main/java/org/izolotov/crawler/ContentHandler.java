package org.izolotov.crawler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

public class ContentHandler /*implements ResponseHandler*/ {

//	private int 
	
//	public ContentHandler() {
//		
//	}
	
//	@Override
//	public void getWebPage(WebPage page, HttpResponse response) {
//		HttpEntity entity = response.getEntity();
//		String contentType = entity.getContentType().getValue();
//		String content = entity != null ? EntityUtils.toString(entity) : null;
//		return new WebPage(url, content, contentType, statusCode);
//	}

//	@Override
	public static void accept(WebPage page, HttpResponse response) {
		HttpEntity entity = response.getEntity();
		String contentType = entity.getContentType().getValue();
//		String content = entity != null ? EntityUtils.toString(entity) : null;
//		return new WebPage(url, content, contentType, statusCode);
	}

}
