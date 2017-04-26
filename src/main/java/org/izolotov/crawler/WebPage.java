package org.izolotov.crawler;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
//import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.izolotov.crawler.fetch.FetchFlag;

import com.google.common.base.Optional;

public class WebPage implements Serializable, HasContent, HasUrl {

    // TODO get the default protocol from config
    private final static String DEFAULT_PROTOCOL = "http://";

    private URL url;
    private Optional<String> content;
    private Optional<String> contentType;
    private Status fetchStatus;
    private int httpStatusCode;

    // TODO pass the config instance
    public static WebPage of(String url) {
        if (url == null) {
            throw new IllegalArgumentException("Url should be specified.");
        }
        URL urlInstance = null;
        try {
            urlInstance = new URL(url);
        } catch (MalformedURLException e) {
            String revisedUrl = null;
            // if there is no protocol or incorrect protocol specified
            if (Pattern.matches("^.*:/+.*$", url)) {
                revisedUrl = url.replaceFirst("^.*:/+", DEFAULT_PROTOCOL);
            } else {
                revisedUrl = DEFAULT_PROTOCOL + url;
            }
            try {
                urlInstance = new URL(revisedUrl);
            } catch (MalformedURLException exc) {
                throw new IllegalArgumentException("Can't construct URL for '%s'.");
            }
        }
        return new WebPage(urlInstance);
    }

    protected WebPage(URL url) {
        this.url = url;
        fetchStatus = new Status(FetchFlag.NOT_FETCHED_YET);
        content = Optional.absent();
        contentType = Optional.absent();
    }

    @Override
    public String getContent() {
//		return content.orElse("");
        return content.or("");
    }

    @Override
    public String getContentType() {
//		return contentType.orElse("");
        return contentType.or("");
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public Status getFetchStatus() {
        return fetchStatus;
    }

    public String getUrlString() {
        return url.toString();
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public void setUrl(URL url) {
        this.url = url;
    }

    public void setContentType(String contentType) {
//		this.contentType = Optional.ofNullable(contentType);
        this.contentType = Optional.fromNullable(contentType);
    }

    public void setContent(String content) {
//		this.content = Optional.ofNullable(content);
        this.content = Optional.fromNullable(content);
    }

    public void setFetchStatus(Status fetchStatus) {
        this.fetchStatus = fetchStatus;
    }

    public void setHttpStatusCode(int code) {
        this.httpStatusCode = code;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).
                append(getUrlString()).
                append(getContent()).
                append(getContentType()).
                append(getHttpStatusCode()).
                append(getFetchStatus().getFlag()).
                toHashCode();
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
        WebPage page = (WebPage) obj;
        return new EqualsBuilder()
//				.appendSuper(super.equals(obj))
                .append(getUrlString(), page.getUrlString())
                .append(getContent(), page.getContent())
                .append(getContentType(), page.getContentType())
                .append(getHttpStatusCode(), page.getHttpStatusCode())
                .append(getFetchStatus().getFlag(), page.getFetchStatus().getFlag())
                .isEquals();
    }

    @Override
    public String toString() {
        return getUrlString() + " " +
                getFetchStatus().getFlag() + " " +
                getHttpStatusCode() + " " +
                getContent() + " " +
                getContentType() + " " +
                getFetchStatus().getFlag();
    }
}
