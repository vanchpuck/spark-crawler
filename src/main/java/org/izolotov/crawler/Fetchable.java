package org.izolotov.crawler;

/**
 * Created by izolotov on 19.04.17.
 */
public interface Fetchable {

    Status getFetchStatus();

    void setFetchStatus(Status status);

}
