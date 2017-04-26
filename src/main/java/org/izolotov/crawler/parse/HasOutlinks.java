package org.izolotov.crawler.parse;

import java.util.Collection;

public interface HasOutlinks {

    public Collection<String> getOutlinks();

    public void setOutlinks(Collection<String> outlinks);

}
