package org.izolotov.crawler.fetch;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.izolotov.crawler.WebPage;

public class RedirectHandler implements ResponseHandler {

    @Override
    public void handle(WebPage page, HttpResponse response) {
        Header locationHeader = response.getFirstHeader("location");
        if (locationHeader == null) {
            FailFlag.INVALID_REDIRECT.setStatus(page,
                    String.format("Received redirect response %s but no location header.", response.getStatusLine()));
            return;
        }
        String location = locationHeader.getValue();
        try {
            URI locationUri = new URI(location);
            String redirectUrl = null;
            if (locationUri.isAbsolute()) {
                redirectUrl = location;
            } else {
                URI baseUri = new URI(page.getUrlString());
                redirectUrl = baseUri.resolve(locationUri).toString();
            }
            FetchFlag.REDIRECT.setStatus(page, redirectUrl);
        } catch (URISyntaxException e) {
            FailFlag.INVALID_REDIRECT.setStatus(page, "Illegal redirect URL.");
        }
    }

}
