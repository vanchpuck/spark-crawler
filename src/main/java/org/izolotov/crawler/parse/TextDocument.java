package org.izolotov.crawler.parse;

import com.google.common.base.Optional;
import org.izolotov.crawler.Status;

import java.util.Collection;
import java.util.HashSet;

public class TextDocument /*extends BaseDocument*/ implements Parsable, HasText, HasOutlinks {

    private Collection<String> outlinks;
    private Status status;
    private String text;

    public TextDocument(DocumentBuilder builder) {
        outlinks = new HashSet<>();
        status = new Status(ParseFlag.FAIL);
        builder.addOutlinks(this).
                setText(this).
                setStatus(this);
    }

    @Override
    public Collection<String> getOutlinks() {
        return outlinks;
    }

    @Override
    public Status getParseStatus() {
        return status;
    }

    @Override
    public Optional<String> getText() {
        return Optional.fromNullable(text);
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }
}
