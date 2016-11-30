package org.izolotov.crawler;

import java.util.Optional;

public class WebPage {

	private String url;
	private Optional<String> content;
	private Optional<String> contentType;
//	private FetchStatus fetchStatus;
	private int status;
	
//	public WebPage(String url) {
//		this.url = url;
//		fetchStatus = FetchStatus.NOT_FETCHED_YET;
//	}
	
	public WebPage(String url, String content, String contentType, int status) {
		this.url = url;
		this.content = Optional.ofNullable(content);
		this.contentType = Optional.ofNullable(contentType);
		this.status = status;
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
	
	public int getStatus() {
		return status;
	}
	
	public String getUrl() {
		return url;
	}
}
