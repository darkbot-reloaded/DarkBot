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
import java.util.function.Consumer;

public class HangarManager {

    private static final Gson GSON = new Gson();
    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final Type DRONE_LIST = new TypeToken<List<Drone>>(){}.getType();

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
                backpageManager.getConnection(url, 2000).getResponseCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.lastChangeHangar = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public Boolean checkDrones() {
        updateHangars();
        updateDrones();
        boolean repaired = !drones.isEmpty();
        for (Drone drone : drones) {
            if (drone.getDamage() / 100d >= main.config.MISCELLANEOUS.REPAIR_DRONE_PERCENTAGE) {
                repaired &= repairDrone(drone);
            }
        }
        return repaired;
    }

    public void updateDrones() {
        String hangarID = getActiveHangar();
        if (hangarID == null) return;

        String encodeParams = Base64Utils.base64Encode("{\"params\":{\"hi\":" + hangarID + "}}");
        String url = "flashAPI/inventory.php?action=getHangar&params="+encodeParams;
        String json = this.backpageManager.getDataInventory(url);

        forEachHangar(json, h -> {
            if (!h.get("hangar_is_active").getAsBoolean()) return;
            this.drones = GSON.fromJson(h.get("general").getAsJsonObject().get("drones"), DRONE_LIST);
        });
    }

    private boolean repairDrone(Drone drone) {
        try {
            String encodeParams = Base64Utils.base64Encode( "{\"action\":\"repairDrone\",\"lootId\":\""
                    + drone.getLoot() + "\",\"repairPrice\":" + drone.getRepairPrice() +
                    ",\"params\":{\"hi\":" + getActiveHangar() + "}," +
                    "\"itemId\":\"" + drone.getItemId() + "\",\"repairCurrency\":\"" + drone.getRepairCurrency() +
                    "\",\"quantity\":1,\"droneLevel\":" + drone.getDroneLevel() + "}");
            String url = "flashAPI/inventory.php?action=repairDrone&params="+encodeParams;
            String json = this.backpageManager.getDataInventory(url);
            return json.contains("'isError':0");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateHangars() {
        String params = "flashAPI/inventory.php?action=getHangarList";
        String hangarData = backpageManager.getDataInventory(params);

        if (hangarData == null) return;

        hangars.clear();
        forEachHangar(hangarData, h -> hangars.add(GSON.fromJson(h, Hangar.class)));
    }

    private void forEachHangar(String json, Consumer<JsonObject> hangarConsumer) {
        try {
            JsonElement hangars = JSON_PARSER.parse(json).getAsJsonObject().get("data").getAsJsonObject()
                    .get("ret").getAsJsonObject().get("hangars");
            if (hangars instanceof JsonArray) {
                hangars.getAsJsonArray().forEach(h -> hangarConsumer.accept(h.getAsJsonObject()));
            } else {
                hangars.getAsJsonObject().entrySet().forEach(h -> hangarConsumer.accept(h.getValue().getAsJsonObject()));
            }
        } catch (Exception e) {
            System.err.println("Failed to iterate hangars: " + json);
            throw e;
        }
    }

    public String getActiveHangar() {
        for (Hangar hangar : hangars) {
            if (hangar.hangarIsActive()) return hangar.getHangarId();
        }
        return null;
    }
}
