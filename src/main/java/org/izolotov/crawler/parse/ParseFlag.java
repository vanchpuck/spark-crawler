package org.izolotov.crawler.parse;

import com.google.common.base.Optional;
import org.izolotov.crawler.Flag;
import org.izolotov.crawler.WebPage;

/**
 * Created by izolotov on 18.04.17.
 */
public enum ParseFlag implements Flag<Parsable> {
    SUCCESS(0),
    FAIL(1);

    private final int code;

    ParseFlag(int code) {
        this.code = code;
    }

    @Override
    public void setStatus(Parsable page) {
        page.getParseStatus().setFlag(this);
    }

    @Override
    public void setStatus(Parsable page, String message) {
        setStatus(page);
        page.getParseStatus().getInfo().put(this.toString(), message);
    }

    @Override
    public Optional<String> getStatusMessage(Parsable page) {
        return Optional.fromNullable(page.getParseStatus().getInfo().get(this.toString()));
    }

    @Override
    public boolean check(Parsable page) {
        return this == page.getParseStatus().getFlag();
    }

    @Override
    public int getCode() {
        return code;
    }
}
