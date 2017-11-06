package org.izolotov.crawler.fetch;

import lombok.Getter;
import org.apache.http.HttpResponse;

/**
 * {@link HttpResponse} wrapper. In addition to response
 * contaions additional request meta info.
 * @param <T>
 */
public class FetchResult<T extends HttpResponse> {

    @Getter private final T response;

    @Getter private final long responseTime;

    public FetchResult(T response, long responseTime) {
        this.response = response;
        this.responseTime = responseTime;
    }

}
