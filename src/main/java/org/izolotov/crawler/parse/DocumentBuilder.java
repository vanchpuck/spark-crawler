package org.izolotov.crawler.parse;

import org.izolotov.crawler.Fetchable;
import org.izolotov.crawler.HasUrl;

public interface DocumentBuilder {

    DocumentBuilder setParseStatus(Parsable  parsable);

    DocumentBuilder setFetchStatus(Fetchable parsable);

    DocumentBuilder setText(HasText  hasText);

    DocumentBuilder setOutlinks(HasOutlinks hasOutlinks);

    DocumentBuilder setUrl(HasUrl hasurl);
}
