package org.izolotov.crawler.parse;

import java.util.Collection;

public interface HasOutlinks {

    Collection<String> getOutlinks();

    void setOutlinks(Collection<String> outlinks);

}
