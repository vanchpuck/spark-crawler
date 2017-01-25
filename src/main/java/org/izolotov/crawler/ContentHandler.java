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
			String contentType = entity.getContentType().getValue();
			//TODO Problems with cp1251 encoding on parse.
			String content = EntityUtils.toString(entity);
			page.setContent(content);
			page.setContentType(contentType);
			Flag.SUCCESS.setStatus(page);
		} catch (Exception e) {
			Flag.SUCCESS.setStatus(page, e.getMessage());
		}
	}

}
