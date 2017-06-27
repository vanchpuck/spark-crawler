package org.izolotov.crawler;

import com.google.common.base.Optional;

public interface Flag<T> {

    void setStatus(T page);

    void setStatus(T page, String message);

    Optional<String> getStatusMessage(T page);

    boolean check(T page);

    int getCode();

}
