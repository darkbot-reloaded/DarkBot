package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.backpage.nova.Agent;
import com.github.manolo8.darkbot.backpage.nova.NovaData;
import com.github.manolo8.darkbot.backpage.nova.Perk;
import com.github.manolo8.darkbot.utils.http.Method;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

public class NovaManager {
    private final BackpageManager backpageManager;
    private final NovaData data;
    private final Gson g;
    private long lastNovaUpdate;

    public NovaManager(BackpageManager backpageManager) {
        this.backpageManager = backpageManager;
        this.data = new NovaData();
        this.g = backpageManager.getGson();
    }

    public boolean update(long expiryTime) {
        try {
            if (System.currentTimeMillis() <= lastNovaUpdate + expiryTime) return false;

            data.getRosterList().forEach((k, v) -> v.setForRemoval(true));

            updateActiveCaptain(0);
            updateResource();
            updateRosterList();

            data.getRosterList().values().removeIf(Agent::getForRemoval);
            lastNovaUpdate = System.currentTimeMillis();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public NovaData getData() {
        return this.data;
    }

    public boolean updateActiveCaptain(int captainId) throws IOException {
        String response = backpageManager.getConnection("ajax/captain.php", Method.POST)
                .setParam("command", "updateActiveCaptain")
                .setParam("captainId", captainId == 0 ? "" : captainId)
                .getContent();

        JsonObject jsonObj = g.fromJson(response, JsonObject.class); //Converts the json string to JsonElement without POJO
        if (jsonObj.get("result").getAsString().equalsIgnoreCase("OK")) {
            this.data.setActiveCaptainId(jsonObj.get("activeCaptainId").getAsString().isEmpty() ? 0 : jsonObj.get("activeCaptainId").getAsInt());
            return true;
        } else {
            System.out.println("NovaManager: " + response);
        }
        return false;
    }

    private void updateResource() throws IOException {
        String response = backpageManager.getConnection("ajax/captain.php", Method.POST)
                .setParam("command", "getResources")
                .getContent();

        JsonObject jsonObj = g.fromJson(response, JsonObject.class); //Converts the json string to JsonElement without POJO
        if (jsonObj.get("result").getAsString().equalsIgnoreCase("OK")) {
            this.data.setResourceAmount(jsonObj.get("item").getAsJsonObject().get("amount").getAsInt());
        } else {
            System.out.println("NovaManager: " + response);
        }
    }

    private void updateRosterList() throws IOException {
        String response = backpageManager.getConnection("ajax/captain.php", Method.POST)
                .setParam("command", "getRosterList")
                .getContent();

        JsonObject jsonObj = g.fromJson(response, JsonObject.class); //Converts the json string to JsonElement without POJO

        if (jsonObj.get("result").getAsString().equalsIgnoreCase("OK")) {
            String resp = jsonObj.getAsJsonArray("rosterList").toString();
            Agent[] rosterList = g.fromJson(resp, Agent[].class);

            for (Agent agent : rosterList) {
                this.data.addAgent(agent);
            }
        } else {
            System.out.println("NovaManager: " + response);
        }
    }

    public boolean dismissAgent(int captainId) throws IOException {
        String response = backpageManager.getConnection("ajax/captain.php", Method.POST)
                .setParam("command", "dismissCaptain")
                .setParam("captainId", captainId)
                .getContent();
        this.update(0);
        JsonObject jsonObj = g.fromJson(response, JsonObject.class); //Converts the json string to JsonElement without POJO
        if (jsonObj.get("result").getAsString().equalsIgnoreCase("OK")) {
            return true;
        } else {
            System.out.println("NovaManager: " + response);
        }
        return false;
    }

    public boolean buyNova(int amount) throws IOException {
        String response = backpageManager.getConnection("ajax/shop.php", Method.POST)
                .setParam("action", "purchase")
                .setParam("category", "special")
                .setParam("itemId", "captain_captain-generic")
                .setParam("amount", amount)
                .setParam("level", "")
                .setParam("selectedName", "")
                .getContent();
        this.update(0);
        JsonObject jsonObj = g.fromJson(response, JsonObject.class);
        if (jsonObj.get("result").getAsString().equalsIgnoreCase("success")) {
            return true;
        } else {
            System.out.println("NovaManager: " + response);
        }
        return false;
    }

    public Perk getPerkDetail(Agent agent, Perk perk) throws IOException {
        String response = backpageManager.getConnection("ajax/captain.php", Method.POST)
                .setParam("command", "getPerkUpgradeDetail")
                .setParam("captainId", agent.getCaptainId())
                .setParam("perkId", perk.getPerkId())
                .getContent();

        JsonObject jsonObj = g.fromJson(response, JsonObject.class); //Converts the json string to JsonElement without POJO
        if (jsonObj.get("result").getAsString().equalsIgnoreCase("OK")) {
            String resp = jsonObj.get("perk").toString();
            return g.fromJson(resp, Perk.class);
        } else {
            System.out.println("NovaManager: " + response);
        }
        return null;
    }

    public boolean upgradeAgentPerk(Agent agent, Perk perk, int upgradeLevel) throws IOException {
        String response = backpageManager.getConnection("ajax/captain.php", Method.POST)
                .setParam("command", "upgradePerk")
                .setParam("captainId", agent.getCaptainId())
                .setParam("perkId", perk.getPerkId())
                .setParam("upgradeLevel", upgradeLevel)
                .getContent();
        this.update(0);
        JsonObject jsonObj = g.fromJson(response, JsonObject.class);
        if (jsonObj.get("result").getAsString().equalsIgnoreCase("OK")) {
            return true;
        } else {
            System.out.println("NovaManager: " + response);
        }
        return false;
    }
}
