package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.backpage.nova.Agent;
import com.github.manolo8.darkbot.backpage.nova.NovaData;
import com.github.manolo8.darkbot.backpage.nova.Perk;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

public class NovaManager {
    private final BackpageManager backpageManager;
    private final NovaData data;
    private final Gson gson;
    private long lastNovaUpdate;

    public NovaManager(BackpageManager backpageManager) {
        this.backpageManager = backpageManager;
        this.data = new NovaData();
        this.gson = backpageManager.getGson();
    }

    /**
     *
     * @param expiryTime only update if within
     * @return null if update wasn't required (non-expired), true if updated ok, false if update failed
     */
    public Boolean update(long expiryTime) {
        try {
            if (System.currentTimeMillis() <= lastNovaUpdate + expiryTime) return null;
            data.getRosterList().forEach((k, v) -> v.setForRemoval(true));

            boolean updated = updateActiveCaptain(0) && updateResource();
            boolean rosterUpdated = updateRosterList();
            if (rosterUpdated) {
                data.getRosterList().values().removeIf(Agent::getForRemoval);
            }
            lastNovaUpdate = System.currentTimeMillis();

            return updated && rosterUpdated;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public NovaData getData() {
        return this.data;
    }

    public boolean updateActiveCaptain(int captainId) throws IOException {
        String response = backpageManager.postHttp("ajax/captain.php")
                .setParam("command", "updateActiveCaptain")
                .setParam("captainId", captainId == 0 ? "" : captainId)
                .getContent();

        JsonObject jsonObj = gson.fromJson(response, JsonObject.class); //Converts the json string to JsonElement without POJO
        if (jsonObj.get("result").getAsString().equalsIgnoreCase("OK")) {
            this.data.setActiveCaptainId(jsonObj.get("activeCaptainId").getAsString().isEmpty() ? 0 : jsonObj.get("activeCaptainId").getAsInt());
            return true;
        } else {
            System.out.println("NovaManager: " + response);
        }
        return false;
    }

    private boolean updateResource() throws IOException {
        String response = backpageManager.postHttp("ajax/captain.php")
                .setParam("command", "getResources")
                .getContent();

        JsonObject jsonObj = gson.fromJson(response, JsonObject.class); //Converts the json string to JsonElement without POJO
        if (jsonObj.get("result").getAsString().equalsIgnoreCase("OK")) {
            this.data.setResourceAmount(jsonObj.get("item").getAsJsonObject().get("amount").getAsInt());
            return true;
        } else {
            System.out.println("NovaManager: " + response);
        }
        return false;
    }

    private boolean updateRosterList() throws IOException {
        String response = backpageManager.postHttp("ajax/captain.php")
                .setParam("command", "getRosterList")
                .getContent();

        JsonObject jsonObj = gson.fromJson(response, JsonObject.class); //Converts the json string to JsonElement without POJO

        if (jsonObj.get("result").getAsString().equalsIgnoreCase("OK")) {
            String resp = jsonObj.getAsJsonArray("rosterList").toString();
            Agent[] rosterList = gson.fromJson(resp, Agent[].class);
            for (Agent agent : rosterList) {
                this.data.addAgent(agent);
            }
            return true;
        } else {
            System.out.println("NovaManager: " + response);
        }
        return false;
    }

    public boolean dismissAgent(int captainId) throws IOException {
        String response = backpageManager.postHttp("ajax/captain.php")
                .setParam("command", "dismissCaptain")
                .setParam("captainId", captainId)
                .getContent();
        this.update(0);
        JsonObject jsonObj = gson.fromJson(response, JsonObject.class); //Converts the json string to JsonElement without POJO
        if (jsonObj.get("result").getAsString().equalsIgnoreCase("OK")) {
            return true;
        } else {
            System.out.println("NovaManager: " + response);
        }
        return false;
    }

    public boolean buyNova(int amount) throws IOException {
        String response = backpageManager.postHttp("ajax/shop.php")
                .setParam("action", "purchase")
                .setParam("category", "special")
                .setParam("itemId", "captain_captain-generic")
                .setParam("amount", amount)
                .setParam("level", "")
                .setParam("selectedName", "")
                .getContent();
        this.update(0);
        JsonObject jsonObj = gson.fromJson(response, JsonObject.class);
        if (jsonObj.get("result").getAsString().equalsIgnoreCase("success")) {
            return true;
        } else {
            System.out.println("NovaManager: " + response);
        }
        return false;
    }

    public Perk getPerkDetail(Agent agent, Perk perk) throws IOException {
        String response = backpageManager.postHttp("ajax/captain.php")
                .setParam("command", "getPerkUpgradeDetail")
                .setParam("captainId", agent.getCaptainId())
                .setParam("perkId", perk.getPerkId())
                .getContent();

        JsonObject jsonObj = gson.fromJson(response, JsonObject.class); //Converts the json string to JsonElement without POJO
        if (jsonObj.get("result").getAsString().equalsIgnoreCase("OK")) {
            String resp = jsonObj.get("perk").toString();
            return gson.fromJson(resp, Perk.class);
        } else {
            System.out.println("NovaManager: " + response);
        }
        return null;
    }

    public boolean upgradeAgentPerk(Agent agent, Perk perk, int upgradeLevel) throws IOException {
        String response = backpageManager.postHttp("ajax/captain.php")
                .setParam("command", "upgradePerk")
                .setParam("captainId", agent.getCaptainId())
                .setParam("perkId", perk.getPerkId())
                .setParam("upgradeLevel", upgradeLevel)
                .getContent();
        this.update(0);
        JsonObject jsonObj = gson.fromJson(response, JsonObject.class);
        if (jsonObj.get("result").getAsString().equalsIgnoreCase("OK")) {
            return true;
        } else {
            System.out.println("NovaManager: " + response);
        }
        return false;
    }
}
