package org.izolotov.crawler.fetch;

import java.io.Serializable;

public class UserAgent implements Serializable{

    private final String agentName;
    private final String info;

    public UserAgent(String agentName) {
        this(agentName, null);
    }

    public UserAgent(String agentName, String info) {
        this.agentName = agentName;
        this.info = info;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getEmail() {
        return info;
    }

    public String getUserAgentString() {
        return agentName + (info != null ? String.format(" (%s)", info) : "");
    }
}
