package org.izolotov.crawler.fetch;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;

public class ResponseUtil {

    private ResponseUtil() {}

    public static int getHttpStatusCode(HttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

    public static String getContentString(HttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

    public static String getContentString(HttpResponse response, Charset charset) throws IOException {
        return EntityUtils.toString(response.getEntity(), charset);
    }

    public static String getContentType(HttpResponse response) {
        return response.getEntity().getContentType().getValue();
    }
}
