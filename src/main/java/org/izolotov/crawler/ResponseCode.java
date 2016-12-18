package org.izolotov.crawler;

public enum ResponseCode {
	OK(200),
	FOUND(302);
	
	private final int code;
	
	private ResponseCode(int code) {
		this.code = code;
	}
	
	public int getIntCode() {
		return code;
	}
}
