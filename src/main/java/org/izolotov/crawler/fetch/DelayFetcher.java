package org.izolotov.crawler.fetch;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.*;

/**
 * This implementation allows to fetch URLs with specified delay between
 * the termination of one fetching and the commencement of the next.
 */
public class DelayFetcher implements Fetcher<CloseableHttpResponse> {

    private static class DelayUrl implements Delayed {
        final String url;
        final long nextFetchTime;

        DelayUrl(String url, long nextFetchTime) {
            this.url = url;
            this.nextFetchTime = nextFetchTime;
        }

        @Override
        public long getDelay(@NotNull TimeUnit unit) {
            long res = nextFetchTime - System.currentTimeMillis();
            return unit.convert(res, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(@NotNull Delayed o) {
            return 0;
        }
    }

    private final CloseableHttpClient httpClient;
    private final DelayQueue<DelayUrl> queue;
    private long nextFetchTime;
    @Getter private long delay;

    /**
     * Creates fetcher with no delay configured
     * @param httpClient {@link CloseableHttpClient} to send http requests
     */
    public DelayFetcher(CloseableHttpClient httpClient) {
        this(httpClient, 0L);
    }

    public DelayFetcher(CloseableHttpClient httpClient, long delay) {
        this.httpClient = httpClient;
        this.queue = new DelayQueue<>();
        this.nextFetchTime = 0;
        this.delay = delay;
    }

    /**
     * Waits the delay time and starts the fetching. Fetching started
     * immediately on first method invocation.
     * @param url URL to fetch
     * @return raw response
     * @throws FetchException
     */
    public FetchResult<CloseableHttpResponse> fetch(@Nonnull String url) throws FetchException {
        Preconditions.checkArgument(!url.isEmpty(), "Empty URL string");
        queue.add(new DelayUrl(url, nextFetchTime));
        try {
            String u = queue.take().url;
            return request(u);
        } catch (Exception exc) {
            throw new FetchException(exc);
        }
        finally {
            nextFetchTime = System.currentTimeMillis() + delay;
        }
    }

    private FetchResult<CloseableHttpResponse> request(String url) throws IOException{
        System.out.println("request to: " + url);
        HttpGet httpGet = new HttpGet(url);
        long startTime = System.currentTimeMillis();
        org.apache.http.client.methods.CloseableHttpResponse response = httpClient.execute(httpGet);
        long elapsedTime = System.currentTimeMillis() - startTime;
        return new FetchResult<>(response, elapsedTime);
    }

    /**
     * Sets the delay between the termination of one
     * execution and the commencement of the next
     * @param delay delay in millis
     */
    public void setDelay(long delay) {
        this.nextFetchTime = nextFetchTime + (delay - this.delay);
        this.delay = delay;

    }

}
