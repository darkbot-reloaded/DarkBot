package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.entities.Drone;
import com.github.manolo8.darkbot.backpage.entities.Hangar;
import com.github.manolo8.darkbot.utils.Base64Utils;
import com.github.manolo8.darkbot.utils.Time;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HangarManager {

    private static final Gson GSON = new Gson();
    private static final Type HANGAR_MAP = new TypeToken<Map<String, Hangar>>(){}.getType();
    private static final Type DRONE_LIST = new TypeToken<List<Drone>>(){}.getType();

    private final Main main;
    private final BackpageManager backpageManager;
    private long lastChangeHangar = 0;
    private Map<String, Hangar> hangars;
    private List<Drone> drones;

    public HangarManager(Main main, BackpageManager backpageManager) {
        this.main = main;
        this.backpageManager = backpageManager;
        this.hangars = new HashMap<>();
        this.drones = new ArrayList<>();
    }

    public boolean changeHangar(String hangarID) {
        if (this.lastChangeHangar <= System.currentTimeMillis() - 40000 && backpageManager.sidStatus().contains("OK")) {
            String url = "indexInternal.es?action=internalDock&subAction=changeHangar&hangarId=" + hangarID;
            try {
                backpageManager.getConnection(url).getResponseCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.lastChangeHangar = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public boolean checkDrones() {
        updateHangars();
        updateDrones();
        boolean repaired = true;
        for (Drone drone : drones) {
            if (drone.getDamage() / 100d >= main.config.MISCELLANEOUS.REPAIR_DRONE_PERCENTAGE) {
                Time.sleep(2000);
                repaired &= repairDrone(drone);
            }
        }
        return repaired;
    }

    public void updateDrones() {
        try {
            String hangarID = getActiveHangar();
            if (hangarID == null) return;

            String encodeParams = Base64Utils.base64Encode("{\"params\":{\"hi\":" + hangarID + "}}");
            String url = "flashAPI/inventory.php?action=getHangar&params="+encodeParams;
            String json = this.backpageManager.getDataInventory(url);

            JsonObject hangars = new JsonParser().parse(json).getAsJsonObject().get("data")
                    .getAsJsonObject().get("ret").getAsJsonObject().get("hangars").getAsJsonObject();

            for (Map.Entry<String, JsonElement> hangar : hangars.entrySet()) {
                JsonObject currHangar = hangar.getValue().getAsJsonObject();
                if (!currHangar.get("hangar_is_active").getAsBoolean()) continue;

                this.drones = GSON.fromJson(currHangar.get("general").getAsJsonObject().get("drones"), DRONE_LIST);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean repairDrone(Drone drone){
        try {
            String encodeParams = Base64Utils.base64Encode( "{\"action\":\"repairDrone\",\"lootId\":\""
                    + drone.getLoot() + "\",\"repairPrice\":" + drone.getRepairPrice() +
                    ",\"params\":{\"hi\":" + getActiveHangar() + "}," +
                    "\"itemId\":\"" + drone.getItemId() + "\",\"repairCurrency\":\"" + drone.getRepairCurrency() +
                    "\",\"quantity\":1,\"droneLevel\":" + drone.getDroneLevel() + "}");
            String url = "flashAPI/inventory.php?action=repairDrone&params="+encodeParams;
            String json = this.backpageManager.getDataInventory(url);
            return json.contains("'isError':0");
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void updateHangars() {
        String params = "flashAPI/inventory.php?action=getHangarList";
        String hangarData = backpageManager.getDataInventory(params);

        if (hangarData == null) return;

        JsonObject hangarsArray = new JsonParser().parse(hangarData).getAsJsonObject().get("data").getAsJsonObject()
                .get("ret").getAsJsonObject().get("hangars").getAsJsonObject();

        this.hangars = GSON.fromJson(hangarsArray, HANGAR_MAP);
    }

    public String getActiveHangar() {
        for (Hangar hangar : hangars.values()) {
            if (hangar.hangarIsActive()) return hangar.getHangarId();
        }
        return null;
    }
}
