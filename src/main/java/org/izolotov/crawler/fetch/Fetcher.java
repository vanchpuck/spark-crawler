package org.izolotov.crawler.fetch;

import org.apache.http.HttpResponse;

public interface Fetcher<T extends HttpResponse> {

    FetchResult<T> fetch(String url) throws FetchException;

}
