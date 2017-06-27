package org.izolotov.crawler.parse;

import com.google.common.base.Optional;
import org.izolotov.crawler.Flag;
import org.izolotov.crawler.Status;

import java.io.Serializable;

public enum ParseFlag implements Flag<Parsable>, Serializable {
    SUCCESS(0),
    FAIL(1),
    META_REDIRECT(2),
    NOT_PARSED(3);

    private final int code;

    ParseFlag(int code) {
        this.code = code;
    }

    public void setMessage(Status status, String message) {
        status.getInfo().put(ParseFlag.class.toString(), message);
    }

    @Override
    public void setStatus(Parsable page) {
        page.getParseStatus().setFlag(this);
    }

    @Override
    public void setStatus(Parsable page, String message) {
        setStatus(page);
        setMessage(page.getParseStatus(), message);
    }

    @Override
    public Optional<String> getStatusMessage(Parsable page) {
        return Optional.fromNullable(page.getParseStatus().getInfo().get(ParseFlag.class.toString()));
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
