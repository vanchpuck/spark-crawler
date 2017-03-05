package org.izolotov.crawler.fetch;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

class ResponseHandlerManager implements Serializable{

    public static class Builder {
        private Map<Integer, ResponseHandler> bHandlersMap;
        private ResponseHandler bDefaultHandler;
        public  Builder(ResponseHandler defaultHandler) {
            this.bDefaultHandler = defaultHandler;
            bHandlersMap = new HashMap<>();
        }

        public Builder setHandler(ResponseHandler handler, int responseCode) {
            bHandlersMap.put(responseCode, handler);
            return this;
        }

        public Builder setHandler(ResponseHandler handler, int ... responseCodes) {
            for (int code : responseCodes) {
                setHandler(handler, code);
            }
            return this;
        }

        public Map<Integer, ResponseHandler> getHandlersMap() {
            return new HashMap<>(bHandlersMap);
        }

        public ResponseHandler geDefaultHandler() {
            return bDefaultHandler;
        }

        public ResponseHandlerManager build() {
            return new ResponseHandlerManager(this);
        }
    }

	private ResponseHandler defaultHandler;
    private Map<Integer, ResponseHandler> handlersMap;

	protected ResponseHandlerManager(Builder builder) {
		defaultHandler = builder.geDefaultHandler();
        handlersMap = builder.getHandlersMap();
	}
	
	public ResponseHandler getHandler(Integer intCode) {
		ResponseHandler handler = handlersMap.get(intCode);
		return handler != null ? handler : defaultHandler;
	}
	
}
