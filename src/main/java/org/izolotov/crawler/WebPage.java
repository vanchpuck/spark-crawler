package org.izolotov.crawler;

import java.util.Optional;

import org.izolotov.crawler.FetchStatus.Flag;

public class WebPage {

	private String url;
	private Optional<String> content;
	private Optional<String> contentType;
	private FetchStatus fetchStatus;
	private int httpStatusCode;
	
	public static WebPage of(String url) {
		return new WebPage(url);
	}
	
	public WebPage(String url) {
		this.url = url;
		fetchStatus = FetchStatus.of(Flag.NOT_FETCHED_YET);
	}
	
	public WebPage(String url, String content, String contentType, int httpStatusCode) {
		this.url = url;
		this.content = Optional.ofNullable(content);
		this.contentType = Optional.ofNullable(contentType);
		this.httpStatusCode = httpStatusCode;
	}
	
	public static WebPage blankPage(String url) {
		return new WebPage(url, null, null, -1);
	}
	
	public String getContent() {
		return content.orElse("");
	}
	
	public String getContentType() {
		return contentType.orElse("");
	}
	
	public int getHttpStatusCode() {
		return httpStatusCode;
	}
	
	public FetchStatus getFetchStatus() {
		return fetchStatus;
	}
	
	public String getUrl() {
		return url;
	}
		
	public void setContentType(String contentType) {
		this.contentType = Optional.ofNullable(contentType);
	}
	
	public void setContent(String content) {
		this.content = Optional.ofNullable(content);
	}
	
	public void setFetchStatus(FetchStatus fetchStatus) {
		this.fetchStatus = fetchStatus;
	}
	
	public void setHttpStatusCode(int code) {
		this.httpStatusCode = code;
	}
}
