package com.github.manolo8.darkbot.backpage.nova;

import java.util.LinkedHashMap;
import java.util.Map;

public class NovaData {
    private String activeCaptainId;
    private String resourceAmount;
    private final Map<Integer, Agent> rosterList = new LinkedHashMap<>();

    public String getActiveCaptainId() {
        return activeCaptainId;
    }

    public void setActiveCaptainId(String activeCaptainId) {
        this.activeCaptainId = activeCaptainId;
    }

    public String getResourceAmount() {
        return resourceAmount;
    }

    public void setResourceAmount(String resourceAmount) {
        this.resourceAmount = resourceAmount;
    }

    public Map<Integer, Agent> getRosterList() {
        return rosterList;
    }

    public Agent getAgent(int id) {
        return this.rosterList.get(id);
    }

    public void addAgent(Agent agent) {
        this.rosterList.put(agent.getCaptainId(), agent);
    }

    public void removeAgent(int id) {
        this.rosterList.remove(id);
    }
}
