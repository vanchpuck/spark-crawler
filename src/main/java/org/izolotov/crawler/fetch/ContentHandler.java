package org.izolotov.crawler.fetch;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.izolotov.crawler.fetch.FetchStatus.Flag;
import org.izolotov.crawler.WebPage;

import java.io.IOException;

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
        } catch (IOException e) {
            System.out.println(e);
            FailFlag.CONNECTION_ISSUE.setStatus(page, e.getMessage());
        }
    }

}
