package org.izolotov.crawler.fetch;

import org.apache.commons.httpclient.HttpStatus;

import java.io.Serializable;

public class DefaultResponseHandlerManager extends ResponseHandlerManager implements Serializable {

    public static class SingletonHolder {
        public static final DefaultResponseHandlerManager HOLDER_INSTANCE = new DefaultResponseHandlerManager();
    }

    public static DefaultResponseHandlerManager getInstance() {
        return SingletonHolder.HOLDER_INSTANCE;
    }

    private DefaultResponseHandlerManager() {
        super(new Builder(new DummyHandler()).
                setHandler(new ContentHandler(),
                        HttpStatus.SC_OK).

                setHandler(new RedirectHandler(),
                        HttpStatus.SC_MULTIPLE_CHOICES,
                        HttpStatus.SC_MOVED_PERMANENTLY,
                        HttpStatus.SC_MOVED_TEMPORARILY,
                        HttpStatus.SC_SEE_OTHER,
                        HttpStatus.SC_USE_PROXY,
                        // TODO HttpStatus class doesn't contain constant for permanent redirect (308 code)
                        HttpStatus.SC_TEMPORARY_REDIRECT).

                setHandler(new NoContentHandler(),
                        HttpStatus.SC_NOT_MODIFIED));
    }
}
