package org.izolotov.crawler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

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
			int statusCode = -1;
		    String content = null;
//		    String contentType = null;
		    String contentType = null;
			
			try(CloseableHttpResponse response = httpClient.execute(httpGet)) {
			    HttpEntity entity = response.getEntity();
			    statusCode = response.getStatusLine().getStatusCode();
//			    contentType = ContentType.get(entity);
//			    System.out.println(contentType.getMimeType());
//			    System.out.println(contentType.getCharset());
//			    System.out.println(contentType.toString());
			    contentType = entity.getContentType().getValue();
			    content = entity != null ? EntityUtils.toString(entity) : null;
			}
			System.out.println("statusCode="+statusCode);
			return new WebPage(url, content, contentType, statusCode);
		}
	}
	
	private class Getter implements Callable<Void> {
		private String url;
		Getter(String url) {
			this.url = url;
		}
		@Override
		public Void call() throws Exception {
			WebPage page = null;
			try {
				page = workersPool.submit(new Worker(url)).get(20L, TimeUnit.SECONDS);
			} catch (Exception e) {
				e.printStackTrace();
				page = new WebPage(url, null, null, 999);
			}			
			fetchedPages.add(page);
			return null;
		}
	}
	
	private final ScheduledExecutorService scheduler;
	private final ExecutorService workersPool;
	private final ExecutorService gettersPool;
	private final Queue<String> workQueue;
	private final List<WebPage> fetchedPages;
	
	private final CloseableHttpClient httpClient;
	
	public PageFetcher(List<String> urls) {
		scheduler = Executors.newScheduledThreadPool(3);
		workersPool = Executors.newFixedThreadPool(10);
		gettersPool = Executors.newFixedThreadPool(10);
		workQueue = new ConcurrentLinkedQueue<>(urls);
		fetchedPages = Collections.synchronizedList(new ArrayList<WebPage>());
		
		httpClient = HttpClients.custom()
		        .setConnectionManager(new PoolingHttpClientConnectionManager())
		        .build();
	}
	
	public PageFetcher(Iterable<String> urls) {
		scheduler = Executors.newScheduledThreadPool(3);
		workersPool = Executors.newFixedThreadPool(10);
		gettersPool = Executors.newFixedThreadPool(10);
		fetchedPages = Collections.synchronizedList(new ArrayList<WebPage>());
		workQueue = new ConcurrentLinkedQueue<>();
		urls.forEach(url -> workQueue.add(url));
		
		httpClient = HttpClients.custom()
		        .setConnectionManager(new PoolingHttpClientConnectionManager())
		        .build();
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
