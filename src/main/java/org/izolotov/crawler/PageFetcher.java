package org.izolotov.crawler;

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

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.izolotov.crawler.FetchStatus.Flag;

// TODO add max connections per host
public class PageFetcher {

	private class Worker implements Callable<WebPage> {
		private HttpGet httpGet;
		private String url;	
		Worker(String url) {
			this.url = url;
			this.httpGet = new HttpGet(url);
		}
		@Override
		public WebPage call() throws Exception {
			System.out.println("request to: "+url);
			WebPage page = WebPage.of(url);
			try(CloseableHttpResponse response = httpClient.execute(httpGet)) {
				int statusCode = response.getStatusLine().getStatusCode();
				handlerManager.getHandler(statusCode).handle(page, response);
				page.setHttpStatusCode(statusCode);
				System.out.println("statusCode="+statusCode);
			}			
			return page;
		}
	}
	
	private class Getter implements Callable<Void> {
		private String url;
		Getter(String url) {
			this.url = url;
		}
		@Override
		public Void call() throws Exception {
			WebPage page = WebPage.of(url);
			try {
				page = workersPool.submit(new Worker(url)).get(20L, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				Flag.FAIL.setStatus(page, String.format("Aborting too long fetch for '%s'.", url));
			}
			fetchedPages.add(page);
			return null;
		}
	}
		
	private final ScheduledExecutorService scheduler;
	private final ExecutorService workersPool;
	private final ThreadPoolExecutor gettersPool;
	private final Queue<String> workQueue;
	private final List<WebPage> fetchedPages;
	
	private final CloseableHttpClient httpClient;
	
	private ResponseHandlerManager handlerManager;
	
	public PageFetcher(Iterable<String> urls) {
		scheduler = Executors.newScheduledThreadPool(3);
		workersPool = Executors.newFixedThreadPool(10);
		gettersPool = new ThreadPoolExecutor(10, 10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
		workQueue = new ConcurrentLinkedQueue<>();
		fetchedPages = Collections.synchronizedList(new ArrayList<WebPage>());
		
		httpClient = HttpClients.custom()
		        .setConnectionManager(new PoolingHttpClientConnectionManager())
		        .setDefaultRequestConfig(RequestConfig.custom().setRedirectsEnabled(false).build())
		        .build();
		
		urls.forEach(url -> workQueue.add(url));
		
		handlerManager = new ResponseHandlerManager(new DummyHandler());
		handlerManager.setHandler(ResponseCode.OK, new ContentHandler());
		handlerManager.setHandler(ResponseCode.FOUND, new RedirectHandler());
	}
	
	public List<WebPage> fetch() {
		scheduler.scheduleAtFixedRate(() -> {
			String nextUrl = workQueue.poll();
			if(nextUrl == null) {
				scheduler.shutdown();
			} else {
				gettersPool.submit(new Getter(nextUrl));
			}
		}, 0L, 1000L, TimeUnit.MILLISECONDS);
		
		try {
			if(scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
				System.out.println("completed");
				workersPool.shutdown();
				gettersPool.shutdown();
			} else {
				System.out.println("forced shutdown");
				scheduler.shutdownNow();
				workersPool.shutdownNow();
				gettersPool.shutdownNow();
				workQueue.forEach((String url) -> fetchedPages.add(new WebPage(url, null, null, 100)));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		workersPool.shutdownNow();
		gettersPool.shutdownNow();
		
		return fetchedPages;		
	}
	
}
