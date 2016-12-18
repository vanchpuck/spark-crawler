package org.izolotov.crawler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.izolotov.crawler.FetchStatus.Flag;

public class ContentHandler implements ResponseHandler {

	@Override
	public void handle(WebPage page, HttpResponse response) {
		try {
			HttpEntity entity = response.getEntity();
			System.out.println("Content encoding: "+entity.getContentEncoding());
			String contentType = entity.getContentType().getValue();
			String content = entity != null ? EntityUtils.toString(entity, "cp1251") : null;
			page.setContent(content);
			page.setContentType(contentType);
			Flag.SUCCESS.setStatus(page);
		} catch (Exception e) {
			Flag.SUCCESS.setStatus(page, e.getMessage());
		}
	}

}
