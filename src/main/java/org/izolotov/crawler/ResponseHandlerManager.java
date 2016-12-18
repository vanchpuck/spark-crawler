package org.izolotov.crawler;

import java.util.HashMap;
import java.util.Map;

class ResponseHandlerManager {

	private Map<Integer, ResponseHandler> handlersMap = new HashMap<>();
	private ResponseHandler defaultHandler;
	
	public ResponseHandlerManager(ResponseHandler defaultHandler) {
		this.defaultHandler = defaultHandler;
	}
	
	public ResponseHandlerManager setHandler(ResponseCode code, ResponseHandler handler) {
		handlersMap.put(code.getIntCode(), handler);
		return this;
	}
	
	public ResponseHandler getHandler(Integer intCode) {
		ResponseHandler handler = handlersMap.get(intCode);
		return handler != null ? handler : defaultHandler;
	}
	
}
