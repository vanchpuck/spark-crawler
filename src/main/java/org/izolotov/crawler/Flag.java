package org.izolotov.crawler;

import com.google.common.base.Optional;

import java.io.Serializable;

/**
 * Created by izolotov on 17.04.17.
 */
public interface Flag<T> extends Serializable {

    void setStatus(T page);

    void setStatus(T page, String message);

    Optional<String> getStatusMessage(T page);

    boolean check(T page);

    int getCode();

}
