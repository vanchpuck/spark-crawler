package org.izolotov.crawler.parse;

import com.google.common.base.Optional;

public interface HasText {

    public Optional<String> getText();

    public void setText(String text);

}
