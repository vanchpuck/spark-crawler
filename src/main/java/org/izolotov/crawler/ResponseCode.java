//package org.izolotov.crawler;
//
//import java.util.function.BiConsumer;
//import java.util.function.Function;
//
//import org.apache.http.HttpResponse;
//
//public enum ResponseCode {
//	OK(200, FetchStatus.SUCCESS, ContentHandler::accept);
//	
//	private final int code;
//	private final FetchStatus fetchStatus;
//	private final ResponseHandler handler;
//	
//	private ResponseCode(int code, FetchStatus fetchStatus, BiConsumer<WebPage, HttpResponse> handler) {
//		this.code = code;
//		this.fetchStatus = fetchStatus;
//		this.handler = handler;
//	}
//	
//	public int getCode() {
//		return code;
//	}
//	
//	public ResponseHandler getHandler() {
//		return handler;
//	}
//}
