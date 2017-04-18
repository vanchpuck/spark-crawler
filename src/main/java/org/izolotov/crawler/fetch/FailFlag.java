package org.izolotov.crawler.fetch;

import com.google.common.base.Optional;
import org.izolotov.crawler.Flag;
import org.izolotov.crawler.WebPage;

import java.io.Serializable;

public enum FailFlag implements Serializable, Flag<WebPage> {
    CONNECTION_ISSUE(10),
    TIMEOUT(11),
    INVALID_REDIRECT(12),
    OTHER(19);

    public static final String FETCH_FAIL_FLAG = "FETCH_FAIL_FLAG";

    private final int intCode;

    FailFlag(int intCode) {
        this.intCode = intCode;
    }

    @Override
    public void setStatus(WebPage page) {
        FetchFlag.FAIL.setStatus(page);
        page.getFetchStatus().putInfo(FETCH_FAIL_FLAG, this.toString());
    }

    @Override
    public void setStatus(WebPage page, String message) {
        FetchFlag.FAIL.setStatus(page, message);
        page.getFetchStatus().putInfo(FETCH_FAIL_FLAG, this.toString());
    }

    @Override
    public int getCode() {
        return intCode;
    }

    @Override
    public boolean check(WebPage page) {
        return page.getFetchStatus().getInfo().containsKey(FETCH_FAIL_FLAG);
    }

    @Override
    public Optional<String> getStatusMessage(WebPage page) {
        return Optional.fromNullable(page.getFetchStatus().getInfo().get(FETCH_FAIL_FLAG));
    }
}
