package org.izolotov.crawler.fetch;

import org.izolotov.crawler.WebPage;

import java.io.Serializable;

public enum FailFlag implements Serializable {
    CONNECTION_ISSUE(10),
    TIMEOUT(11),
    INVALID_REDIRECT(12),
    OTHER(19);

    public static final String FAIL_FLAG = "FAIL_FLAG";

    private final int intCode;

    FailFlag(int intCode) {
        this.intCode = intCode;
    }

    public void setStatus(WebPage page) {
        FetchStatus.Flag.FAIL.setStatus(page);
        page.getFetchStatus().putInfo(FAIL_FLAG, this.toString());
    }

    public void setStatus(WebPage page, String message) {
        FetchStatus.Flag.FAIL.setStatus(page, message);
        page.getFetchStatus().putInfo(FAIL_FLAG, this.toString());
    }

    public int getIntCode() {
        return intCode;
    }
}
