package org.izolotov.crawler.parse;

import org.izolotov.crawler.Status;

public interface Parsable {

    Status getParseStatus();

    void setParseStatus(Status status);

}
