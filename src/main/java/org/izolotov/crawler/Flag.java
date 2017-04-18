package org.izolotov.crawler;

import com.google.common.base.Optional;

/**
 * Created by izolotov on 17.04.17.
 */
public interface Flag {

    void setStatus(WebPage page);

    void setStatus(WebPage page, String message);

    Optional<String> getStatusMessage(WebPage page);

    boolean check(WebPage page);

    int getCode();

}
