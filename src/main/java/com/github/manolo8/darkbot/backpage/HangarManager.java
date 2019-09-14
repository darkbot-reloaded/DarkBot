package com.github.manolo8.darkbot.backpage;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.entities.*;
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
    private static final Type ITEMINFO_LIST = new TypeToken<List<ItemInfo>>(){}.getType();
    private static final Type ITEM_LIST = new TypeToken<List<Item>>(){}.getType();
    private static final Type SHIPINFO_LIST = new TypeToken<List<ShipInfo>>(){}.getType();

    private final Main main;
    private final BackpageManager backpageManager;
    private long lastChangeHangar = 0;
    private List<Hangar> hangars;
    private List<Drone> drones;
    private List<Item> items;
    private List<ItemInfo> itemInfos;
    private List<ShipInfo> shipInfos;
    private long lastUpdateHangarData = 0;

    public HangarManager(Main main, BackpageManager backpageManager) {
        this.main = main;
        this.backpageManager = backpageManager;
        this.hangars = new ArrayList<>();
        this.drones = new ArrayList<>();
        this.items = new ArrayList<>();
        this.itemInfos = new ArrayList<>();
        this.shipInfos = new ArrayList<>();
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
        updateHangarData();
        boolean repaired = !drones.isEmpty();
        for (Drone drone : drones) {
            if (drone.getDamage() / 100d >= main.config.MISCELLANEOUS.REPAIR_DRONE_PERCENTAGE) {
                repaired &= repairDrone(drone);
            }
        }
        return repaired;
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
        forEachRet(hangarData, h -> hangars.add(GSON.fromJson(h, Hangar.class)),"hangars");
    }

    public String getActiveHangar() {
        for (Hangar hangar : hangars) {
            if (hangar.hangarIsActive()) return hangar.getHangarId();
        }
        return null;
    }

    public void updateHangarData(int maxExpiryTime){
        if (System.currentTimeMillis() > lastUpdateHangarData + maxExpiryTime){
            updateHangarData();
        }
    }

    public void updateHangarData() {
        updateHangars();
        String hangarID = getActiveHangar();
        if (hangarID == null) return;

        String encodeParams = Base64Utils.base64Encode("{\"params\":{\"hi\":" + hangarID + "}}");
        String json = this.backpageManager.getDataInventory("flashAPI/inventory.php?action=getHangar&params=" + encodeParams);

        if (json != null) {
            updateDrones(json);
            updateItems(json);
            updateItemInfos(json);
            updateShipsInfo(json);
            lastUpdateHangarData = System.currentTimeMillis();
        }
    }

    private void updateDrones(String json) {
        forEachRet(json, h -> {
            if (!h.get("hangar_is_active").getAsBoolean()) return;
            this.drones = GSON.fromJson(h.get("general").getAsJsonObject().get("drones"), DRONE_LIST);
        },"hangars");
    }

    private void updateItemInfos(String json) {
        JsonArray itemInfoArr = JSON_PARSER.parse(json).getAsJsonObject().get("data").getAsJsonObject().get("ret").getAsJsonObject().get("itemInfo").getAsJsonArray();
        this.itemInfos = GSON.fromJson(itemInfoArr, ITEMINFO_LIST);
    }

    private void updateItems(String json) {
        JsonArray itemInfoArr = JSON_PARSER.parse(json).getAsJsonObject().get("data").getAsJsonObject().get("ret").getAsJsonObject().get("items").getAsJsonArray();
        this.items = GSON.fromJson(itemInfoArr, ITEM_LIST);
    }

    private void updateShipsInfo(String json) {
        JsonArray itemInfoArr = JSON_PARSER.parse(json).getAsJsonObject().get("data").getAsJsonObject().get("ret").getAsJsonObject().get("shipInfo").getAsJsonArray();
        this.shipInfos = GSON.fromJson(itemInfoArr, SHIPINFO_LIST);
    }

    private void forEachRet(String json, Consumer<JsonObject> consumer, String key) {
        try {
            JsonElement val = JSON_PARSER.parse(json).getAsJsonObject().get("data").getAsJsonObject()
                    .get("ret").getAsJsonObject().get(key);
            if (val instanceof JsonArray) {
                val.getAsJsonArray().forEach(i -> consumer.accept(i.getAsJsonObject()));
            } else {
                val.getAsJsonObject().entrySet().forEach(i -> consumer.accept(((JsonElement)i.getValue()).getAsJsonObject()));
            }
        } catch (Exception e) {
            System.err.println("Failed to iterate " + key + ": " + json);
            throw e;
        }
    }

    public List<Drone> getDrones() {
        return drones;
    }

    public List<Item> getItems() {
        return items;
    }

    public List<ItemInfo> getItemInfos() {
        return itemInfos;
    }

    public List<ShipInfo> getShipInfos() {
        return shipInfos;
    }
}
