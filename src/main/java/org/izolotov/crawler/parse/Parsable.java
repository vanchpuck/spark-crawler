package org.izolotov.crawler.parse;

import org.izolotov.crawler.Status;

/**
 * Created by izolotov on 17.04.17.
 */
public interface Parsable {

    Status getParseStatus();

    void setParseStatus(Status status);

}
