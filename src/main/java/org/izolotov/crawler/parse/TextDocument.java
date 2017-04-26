package org.izolotov.crawler.parse;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.izolotov.crawler.*;

import java.io.Serializable;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

public class TextDocument /*extends BaseDocument*/ implements Serializable, Parsable, Fetchable, HasText, HasOutlinks, HasUrl {

    private URL url;
    private Collection<String> outlinks;
    private Status parseStatus;
    private Status fetchStatus;
    private String text;


    public TextDocument(DocumentBuilder builder) {
        builder.setOutlinks(this).setText(this).setParseStatus(this).setUrl(this);
    }

    @Override
    public Collection<String> getOutlinks() {
        return outlinks;
    }

    @Override
    public void setOutlinks(Collection<String> outlinks) {
        this.outlinks = outlinks;
    }

    @Override
    public Status getParseStatus() {
        return parseStatus;
    }

    @Override
    public void setParseStatus(Status status) {
        this.parseStatus = status;
    }

    @Override
    public Optional<String> getText() {
        return Optional.fromNullable(text);
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public Status getFetchStatus() {
        return null;
    }

    @Override
    public void setFetchStatus(Status status) {

    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).
                append(getOutlinks()).
                append(getParseStatus().getFlag()).
                append(getText()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        TextDocument doc = (TextDocument) obj;
        return new EqualsBuilder().append(getOutlinks(), doc.getOutlinks())
                .append(getParseStatus().getFlag(), doc.getParseStatus().getFlag())
                .append(getText(), doc.getText())
                .isEquals();
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
