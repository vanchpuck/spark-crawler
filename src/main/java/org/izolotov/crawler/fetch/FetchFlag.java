package org.izolotov.crawler.fetch;

import com.google.common.base.Optional;
import org.izolotov.crawler.Flag;
import org.izolotov.crawler.WebPage;

public enum FetchFlag implements Flag<WebPage> {

    NOT_FETCHED_YET(0),
    SUCCESS(1),
    REDIRECT(2),
    FAIL(3);

    private int intCode;

    FetchFlag(int code) {
        intCode = code;
    }

    public void setStatus(WebPage page) {
        page.getFetchStatus().setFlag(this);
    }

    public void setStatus(WebPage page, String message) {
        setStatus(page);
        page.getFetchStatus().getInfo().put(FetchFlag.class.getName(), message);
    }

    public Optional<String> getStatusMessage(WebPage page) {
        return Optional.fromNullable(page.getFetchStatus().getInfo().get(FetchFlag.class.getName()));
    }

    public boolean check(WebPage page) {
        return this == page.getFetchStatus().getFlag();
    }

    public int getCode() {
        return intCode;
    }
}
