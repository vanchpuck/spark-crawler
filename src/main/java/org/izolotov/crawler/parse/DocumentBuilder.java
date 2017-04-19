package org.izolotov.crawler.parse;

import com.google.common.base.Optional;

import java.util.Collection;

/**
 * Created by izolotov on 19.04.17.
 */
public interface DocumentBuilder {

    public DocumentBuilder setStatus(Parsable  parsable);

    public DocumentBuilder setText(HasText  hasText);

    public DocumentBuilder addOutlinks(HasOutlinks hasOutlinks);

}
