package org.izolotov.crawler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FetchStatus {
	
	public enum Flag {
		NOT_FETCHED_YET,
		SUCCESS,
		REDIRECT,
		FAIL;
		
		public void setStatus(WebPage page) {
			page.getFetchStatus().setCode(this);
		}
		
		public void setStatus(WebPage page, String message) {
			page.getFetchStatus().setCode(this);
			page.getFetchStatus().getInfo().put(this.toString(), message);
		}
		
		public Optional<String> getStatusMessage(WebPage page) {
			return Optional.ofNullable(page.getFetchStatus().getInfo().get(this.toString()));
		}
		
		@Override
		public String toString() {
			return name();
		}
	}
	
	private Flag code;
	private Map<String, String> info;
	
	public static final FetchStatus of(Flag code) {
		return new FetchStatus(code);
	}
	
	protected FetchStatus(Flag code) {
		this.code = code;
		this.info = new HashMap<>(4);
	}
	
	public void setCode(Flag code) {
		this.code = code;
	}
	
	public Flag getCode() {
		return code;
	}
	
	public FetchStatus putInfo(String key, String info) {
		this.info.put(key, info);
		return this;
	}
	
	public Map<String, String> getInfo() {
		return info;
	}
}
