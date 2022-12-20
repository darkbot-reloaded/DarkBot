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
        this.g = new Gson();
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


    /**
     * This method is used to unequip, equip and get current captain
     * @param id
     * @throws IOException
     */
    public boolean updateActiveCaptain(int id) throws IOException {
        String response = backpageManager.getConnection("ajax/captain.php", Method.POST)
                .setRawParam("command", "updateActiveCaptain")
                .setRawParam("captainId", id == 0 ? "" : id)
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

    /**
     * Method to get resources count
     * @throws IOException
     */

    private void updateResource() throws IOException {
        String response = backpageManager.getConnection("ajax/captain.php", Method.POST)
                .setRawParam("command", "getResources")
                .getContent();

        JsonObject jsonObj = g.fromJson(response, JsonObject.class); //Converts the json string to JsonElement without POJO
        if (jsonObj.get("result").getAsString().equalsIgnoreCase("OK")) {
            this.data.setResourceAmount(jsonObj.get("item").getAsJsonObject().get("amount").getAsInt());
        } else {
            System.out.println("NovaManager: " + response);
        }
    }

    /**
     * Method to get list of Nova Agent/Captain
     * @throws IOException
     */

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


    /**
     * Method to dismiss Nova Agent/Captain
     * @param id
     * @return
     * @throws IOException
     */

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

    /**
     * Method to buy Nova Agent/Captain
     * @param amount
     * @return
     * @throws IOException
     */

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

    /**
     * Method to get Nova Agent/Captain's perk upgrade details
     * @param agent
     * @param perk
     * @return
     * @throws IOException
     */
    public Perk getPerkDetail(Agent agent, Perk perk) throws IOException {
        String response = backpageManager.getConnection("ajax/captain.php", Method.POST)
                .setRawParam("command", "getPerkUpgradeDetail")
                .setRawParam("captainId", agent.getCaptainId())
                .setRawParam("perkId", perk.getPerkId())
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

    /**
     * Method to upgrade Nova Agent/Captain Specific Perk
     * @param agent
     * @param perk
     * @param upgradeLevel
     * @return
     * @throws IOException
     */

    public boolean upgradeAgentPerk(Agent agent, Perk perk, int upgradeLevel) throws IOException {
        String response = backpageManager.getConnection("ajax/captain.php", Method.POST)
                .setRawParam("command", "upgradePerk")
                .setRawParam("captainId", agent.getCaptainId())
                .setRawParam("perkId", perk.getPerkId())
                .setRawParam("upgradeLevel", upgradeLevel)
                .getContent();
        this.update(0);
        JsonObject jsonObj = g.fromJson(response, JsonObject.class);
        return jsonObj.get("result").getAsString().equalsIgnoreCase("OK");
    }
}
