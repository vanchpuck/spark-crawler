package org.izolotov.crawler.fetch;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.izolotov.crawler.fetch.FetchStatus.Flag;
import org.izolotov.crawler.WebPage;

public class PageFetcher implements Serializable {

    public static class Builder implements Serializable {

        public static final int MAX_CONNECTIONS_DEFAULT = 5;
        public static final int CONNECTION_TIMEOUT = 10000;
        public static final int SOCKET_TIMEOUT = 4000;
        public static final long MIN_DELAY_DEFAULT = 5000;
        public static final long CONNECTION_TIME_LIMIT = 15000;

        private ResponseHandlerManager bHandlerManager;
        private int bMaxConnections;
        private int bConnectionTimeout;
        private int bSocketTimeout;
        private long bMinDelay;
        private long bConnectionTimeLimit;

        public Builder() {
            this(DefaultResponseHandlerManager.getInstance());
        }

        public Builder(ResponseHandlerManager handlerManager) {
            bHandlerManager = handlerManager;
            bMaxConnections = MAX_CONNECTIONS_DEFAULT;
            bMinDelay = MIN_DELAY_DEFAULT;
            bConnectionTimeout = CONNECTION_TIMEOUT;
            bSocketTimeout = SOCKET_TIMEOUT;
            bConnectionTimeLimit = CONNECTION_TIME_LIMIT;
        }

        public Builder setMaxConnections(int maxConnections) {
            bMaxConnections = maxConnections;
            return this;
        }

        public Builder setMinDelay(long minDelay) {
            bMinDelay = minDelay;
            return this;
        }

        public Builder setConnectionTimeout(int connectionTimeout) {
            bConnectionTimeout = connectionTimeout;
            return this;
        }

        public Builder setSocketTimeout(int sockettimeout) {
            bSocketTimeout = sockettimeout;
            return this;
        }

        public Builder setConnectionTimeLimit(long connectionTimeLimit) {
            bConnectionTimeLimit = connectionTimeLimit;
            return this;
        }


        public ResponseHandlerManager getHandlerManager() {
            return bHandlerManager;
        }

        public int getSocketTimeout() {
            return bSocketTimeout;
        }

        public int getMaxConnections() {
            return bMaxConnections;
        }

        public int getConnectionTimeout() {
            return bConnectionTimeout;
        }

        public long getConnectionTimeLimit() {
            return bConnectionTimeLimit;
        }

        public long getMinDelay() {
            return bMinDelay;
        }

        public PageFetcher build() {
            return new PageFetcher(this);
        }

    }

    private class Worker implements Callable<WebPage> {
        private HttpGet httpGet;
        private WebPage page;

        Worker(WebPage page, HttpGet httpGet) {
            this.page = page;
            this.httpGet = httpGet;
        }

        @Override
        public WebPage call() throws Exception {
            System.out.println("request to: " + page.getUrlString());
            long startTime = System.currentTimeMillis();
            long endTime = startTime;
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                endTime = System.currentTimeMillis();
                int statusCode = response.getStatusLine().getStatusCode();
                handlerManager.getHandler(statusCode).handle(page, response);
                page.setHttpStatusCode(statusCode);
                System.out.println("statusCode=" + statusCode);
            } catch (IOException e) {
                endTime = System.currentTimeMillis();
                FailFlag.CONNECTION_ISSUE.setStatus(page, String.format("Request to %s failed: %s.", page.getUrlString(), e));
            } catch (Exception e) {
                endTime = System.currentTimeMillis();
                String message = String.format("Request to %s has been failed: %s.", page.getUrlString(), e);
                FailFlag.OTHER.setStatus(page, String.format("Request to %s failed: %s.", page.getUrlString(), e));
                System.out.println(message);
            } finally {
                page.getFetchStatus().putInfo(RESPONSE_TIME, String.valueOf(endTime - startTime));
            }
            return page;
        }
    }

    private class Executor implements Callable<Void> {
        private WebPage page;

        Executor(WebPage page) {
            this.page = page;
        }

        @Override
        public Void call() throws Exception {
            HttpGet httpGet = new HttpGet(page.getUrlString());
            try {
                page = workerPool.get().submit(new Worker(page, httpGet)).get(connectionTimeLimit, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                httpGet.abort();
                FailFlag.TIMEOUT.setStatus(page, String.format("Aborting too long fetch of '%s'.", page.getUrlString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            fetchedPages.add(page);
            return null;
        }
    }

    public static final String RESPONSE_TIME = "RESPONSE_TIME";

    private ScheduledExecutorService scheduler;
    private AtomicReference<ExecutorService> workerPool;
    private AtomicReference<ThreadPoolExecutor> executorPool;
    private final Queue<WebPage> workQueue;
    private final List<WebPage> fetchedPages;

    private final CloseableHttpClient httpClient;

    private ResponseHandlerManager handlerManager;
    private long minDelay;
    private long connectionTimeLimit;
    private int maxConnections;

    protected PageFetcher(Builder builder) {
        minDelay = builder.getMinDelay();
        connectionTimeLimit = builder.getConnectionTimeLimit();
        maxConnections = builder.getMaxConnections();

        handlerManager = builder.getHandlerManager();

        httpClient = HttpClients.custom()
                .setConnectionManager(new PoolingHttpClientConnectionManager())
                .setDefaultRequestConfig(RequestConfig.custom().
                        setRedirectsEnabled(false).
                        setConnectionRequestTimeout(builder.getConnectionTimeout()).
                        setConnectTimeout(builder.getConnectionTimeout()).
                        setSocketTimeout(builder.getSocketTimeout()).
                        build())
                .build();

        workQueue = new ConcurrentLinkedQueue<>();
        fetchedPages = Collections.synchronizedList(new ArrayList<WebPage>());
    }

    public List<WebPage> fetch(Iterable<WebPage> urls, long fetchTimeLimit) {
        scheduler = Executors.newScheduledThreadPool(3);
        workerPool = new AtomicReference<>(Executors.newFixedThreadPool(maxConnections));
        executorPool = new AtomicReference<>(new ThreadPoolExecutor(maxConnections, maxConnections,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>()));
        workQueue.clear();
        fetchedPages.clear();

        urls.forEach(url -> workQueue.add(url));

        scheduler.scheduleAtFixedRate(() -> {
            WebPage nextUrl = workQueue.poll();
            if (nextUrl == null) {
                System.out.println("Next url is null");
                scheduler.shutdown();
            } else {
                executorPool.get().submit(new Executor(nextUrl));
            }
        }, 0L, minDelay, TimeUnit.MILLISECONDS);

        try {
            if (scheduler.awaitTermination(fetchTimeLimit, TimeUnit.MILLISECONDS)) {
                System.out.println("completed");
                executorPool.get().shutdown();
                if (executorPool.get().awaitTermination(connectionTimeLimit, TimeUnit.MILLISECONDS)) {
                    workerPool.get().shutdown();
                } else {
                    executorPool.get().shutdownNow();
                    workerPool.get().shutdownNow();
                }
                System.out.println("terminated");
            } else {
                System.out.println("forced shutdown");
                scheduler.shutdownNow();
                workerPool.get().shutdownNow();
                executorPool.get().shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workQueue.forEach((WebPage page) -> fetchedPages.add(page));
        }

        workerPool.get().shutdownNow();
        executorPool.get().shutdownNow();

        return fetchedPages;
    }

    public List<WebPage> fetch(Iterable<WebPage> urls) {
        return fetch(urls, Long.MAX_VALUE);
    }

    public long getMinDelay() {
        return minDelay;
    }

    public long getConnectionTimeLimit() {
        return connectionTimeLimit;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

}
