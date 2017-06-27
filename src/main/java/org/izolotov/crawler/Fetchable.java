package org.izolotov.crawler;

public interface Fetchable {

    Status getFetchStatus();

    void setFetchStatus(Status status);

}
