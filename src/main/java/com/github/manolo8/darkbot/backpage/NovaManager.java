package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.backpage.nova.Agent;
import com.github.manolo8.darkbot.backpage.nova.NovaData;
import com.github.manolo8.darkbot.utils.http.Method;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

public class NovaManager {
    private final BackpageManager backpageManager;
    private final NovaData data;
    private long lastNovaUpdate;
    private final Gson g;

    public NovaManager(BackpageManager backpageManager) {
        this.backpageManager = backpageManager;
        this.data = new NovaData();
        this.g = new Gson();
    }

    public boolean update(int expiryTime) {
        try {
            if (System.currentTimeMillis() <= lastNovaUpdate + expiryTime) return false;

            data.getRosterList().forEach((k, v) -> v.setForRemoval(true));

            updateActiveCaptain();
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

    private void updateActiveCaptain() throws IOException {
        String response = backpageManager.getConnection("ajax/captain.php", Method.POST)
                .setRawParam("command", "updateActiveCaptain")
                .setRawParam("captainId", "")
                .getContent();

        JsonObject jsonObj = g.fromJson(response, JsonObject.class); //Converts the json string to JsonElement without POJO
        if (jsonObj.get("result").getAsString().equalsIgnoreCase("OK")) {
            this.data.setActiveCaptainId(jsonObj.get("activeCaptainId").getAsString());
        }else {
            System.out.println("NovaManager: " + response);
        }

    }

    private void updateResource() throws IOException {
        String response = backpageManager.getConnection("ajax/captain.php", Method.POST)
                .setRawParam("command", "getResources")
                .getContent();

        JsonObject jsonObj = g.fromJson(response, JsonObject.class); //Converts the json string to JsonElement without POJO
        if (jsonObj.get("result").getAsString().equalsIgnoreCase("OK")) {
            this.data.setResourceAmount(jsonObj.get("item").getAsJsonObject().get("amount").getAsString());
        } else {
            System.out.println("NovaManager: " + response);
        }
    }

    private void updateRosterList() throws IOException {
        String response = backpageManager.getConnection("ajax/captain.php", Method.POST)
                .setRawParam("command", "getRosterList")
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

    public boolean equipAgent(int id) throws IOException {
        String response = backpageManager.getConnection("ajax/captain.php", Method.POST)
                .setRawParam("command", "updateActiveCaptain")
                .setRawParam("captainId", id)
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

    public boolean dismissAgent(int id) throws IOException {
        String response = backpageManager.getConnection("ajax/captain.php", Method.POST)
                .setRawParam("command", "dismissCaptain")
                .setRawParam("captainId", id)
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
                .setRawParam("action", "purchase")
                .setRawParam("category", "special")
                .setRawParam("itemId", "captain_captain-generic")
                .setRawParam("amount", amount)
                .setRawParam("level", "")
                .setRawParam("selectedName", "")
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

    public Agent.Perk getPerkDetail(Agent agent, Agent.Perk perk) throws IOException {
        String response = backpageManager.getConnection("ajax/captain.php", Method.POST)
                .setRawParam("command", "getPerkUpgradeDetail")
                .setRawParam("captainId", agent.getCaptainId())
                .setRawParam("perkId", perk.getPerkId())
                .getContent();

        JsonObject jsonObj = g.fromJson(response, JsonObject.class); //Converts the json string to JsonElement without POJO
        if (jsonObj.get("result").getAsString().equalsIgnoreCase("OK")) {
            String resp = jsonObj.get("perk").toString();
            return g.fromJson(resp, Agent.Perk.class);
        } else {
            System.out.println("NovaManager: " + response);
        }
        return null;
    }

    public boolean upgradeAgentPerk(Agent agent, Agent.Perk perk, int upgradeLevel) throws IOException {
        String response = backpageManager.getConnection("ajax/captain.php", Method.POST)
                .setRawParam("command", "upgradePerk")
                .setRawParam("captainId", agent.getCaptainId())
                .setRawParam("perkId", perk.getPerkId())
                .setRawParam("upgradeLevel", upgradeLevel)
                .getContent();
        this.update(0);
        JsonObject jsonObj = g.fromJson(response, JsonObject.class); //Converts the json string to JsonElement without POJO
        return jsonObj.get("result").getAsString().equalsIgnoreCase("OK");
    }
}
