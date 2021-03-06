package org.izolotov.crawler;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@ToString
@EqualsAndHashCode(exclude = "info")
public class Status implements Serializable {
    private Flag flag;
    private Map<String, String> info;

    public static final Status of(Flag flag) {
        return new Status(flag);
    }

    public Status(Flag flag) {
        this.flag = flag;
        this.info = new HashMap<>(4);
    }

    public void setFlag(Flag code) {
        this.flag = code;
    }

    public Flag getFlag() {
        return flag;
    }

    public Status putInfo(String key, String info) {
        this.info.put(key, info);
        return this;
    }

    public Map<String, String> getInfo() {
        return info;
    }
}
