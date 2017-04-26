package org.izolotov.crawler.parse;

import com.google.common.base.Optional;
import org.izolotov.crawler.Fetchable;
import org.izolotov.crawler.HasUrl;

import java.util.Collection;

/**
 * Created by izolotov on 19.04.17.
 */
public interface DocumentBuilder {

    DocumentBuilder setParseStatus(Parsable  parsable);

    DocumentBuilder setFetchStatus(Fetchable parsable);

    DocumentBuilder setText(HasText  hasText);

    DocumentBuilder setOutlinks(HasOutlinks hasOutlinks);

    DocumentBuilder setUrl(HasUrl hasurl);
}
