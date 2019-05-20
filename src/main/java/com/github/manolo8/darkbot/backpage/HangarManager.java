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
import java.util.Collections;
import java.util.List;

public class HangarManager {

    private static final Gson GSON = new Gson();
    private static final Type HANGAR_LIST = new TypeToken<ArrayList<Hangar>>(){}.getType();
    private static final Type DRONE_LIST = new TypeToken<ArrayList<Drone>>(){}.getType();

    private final Main main;
    private final BackpageManager backpageManager;
    private long lastChangeHangar = 0;
    private List<Hangar> hangars;
    private List<Drone> drones;

    public HangarManager(Main main, BackpageManager backpageManager) {
        this.main = main;
        this.backpageManager = backpageManager;
        this.hangars = new ArrayList<>();
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
                repaired &= repairDrone(drone);
                Time.sleep(2000);
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

            JsonElement element = new JsonParser().parse(json).getAsJsonObject().get("data")
                    .getAsJsonObject().get("ret").getAsJsonObject().get("hangars");

            for (JsonElement hangar : (element.isJsonArray() ? element.getAsJsonArray() : Collections.singleton(element))) {
                if (!hangar.getAsJsonObject().get("hangar_is_active").getAsBoolean()) continue;

                JsonArray dronesArray = hangar.getAsJsonObject().get("general").getAsJsonObject().get("drones").getAsJsonArray();
                this.drones = GSON.fromJson(dronesArray, DRONE_LIST);
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

        JsonArray hangarsArray =  new JsonParser().parse(hangarData).getAsJsonObject().get("data").getAsJsonObject()
                .get("ret").getAsJsonObject().get("hangars").getAsJsonArray();
        this.hangars = GSON.fromJson(hangarsArray, HANGAR_LIST);
    }

    public String getActiveHangar() {
        for (Hangar hangar : hangars) {
            if (hangar.hangarIsActive()) return hangar.getHangarId();
        }
        return null;
    }
}
