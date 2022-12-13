package com.github.manolo8.darkbot.backpage.nova;

import java.util.LinkedHashMap;
import java.util.Map;

public class NovaData {
    private final Map<Integer, Agent> rosterList = new LinkedHashMap<>();
    private int activeCaptainId;
    private int resourceAmount;

    public int getActiveCaptainId() {
        return activeCaptainId;
    }

    public void setActiveCaptainId(int activeCaptainId) {
        this.activeCaptainId = activeCaptainId;
    }

    public int getResourceAmount() {
        return resourceAmount;
    }

    public void setResourceAmount(int resourceAmount) {
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
