package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.BackpageManager;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.Drone;
import com.github.manolo8.darkbot.core.entities.Hangar;
import com.github.manolo8.darkbot.utils.Base64Utils;
import com.google.gson.*;

import java.util.ArrayList;

public class HangarManager {

    private final Main main;
    private final BackpageManager backpageManager;
    private static final Gson GSON = new Gson();
    private long lastChangeHangar = 0;
    private ArrayList<Hangar> hangars;
    private ArrayList<Drone> drones;

    public HangarManager(Main main){
        this.main = main;
        this.backpageManager = main.backpage;
        this.hangars = new ArrayList<Hangar>();
        this.drones = new ArrayList<Drone>();
    }

    public boolean changeHangar(String hangarID) {
        if (this.lastChangeHangar <= System.currentTimeMillis() - 40000 && this.main.backpage.sidStatus().contains("OK")) {
            String url = "indexInternal.es?action=internalDock&subAction=changeHangar&hangarId=" + hangarID;
            try {
                backpageManager.getConnection(url).getResponseCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.lastChangeHangar = System.currentTimeMillis();
        } else {
            return false;
        }

        return true;
    }

    public void checkDrones() {
        updateHangars();
        updateDrones();
        for (Drone drone : drones){
            if ((drone.getDamage()/100) >= this.main.config.MISCELLANEOUS.REPAIR_DRONE_PERCENTAGE){
                repairDrone(drone);
                System.out.println("Drone Repair");
            }
        }
    }

    public void updateDrones() {
        try {
            String hangarID = getActiveHangar();

            if (hangarID != null) {
                String encodeParams = Base64Utils.base64Encode("{\"params\":{\"hi\":" + hangarID + "}}");
                String url = "flashAPI/inventory.php?action=getHangar&params="+encodeParams;
                String json = this.backpageManager.getDataInventory(url);

                JsonElement element = new JsonParser().parse(json).getAsJsonObject().get("data")
                        .getAsJsonObject().get("ret").getAsJsonObject().get("hangars");

                if (element.isJsonArray()) {
                    for (JsonElement hangar : element.getAsJsonArray()) {
                        if (hangar.getAsJsonObject().get("hangar_is_active").getAsBoolean()) {
                            JsonArray dronesArray = hangar.getAsJsonObject().get("general").getAsJsonObject().get("drones").getAsJsonArray();
                            for (JsonElement dron : dronesArray){
                                this.drones.add(GSON.fromJson(dron,Drone.class));
                            }
                        }
                    }
                } else {
                    if (element.getAsJsonObject().get("hangar_is_active").getAsBoolean()) {
                        JsonArray dronesArray = element.getAsJsonObject().get("general").getAsJsonObject().get("drones").getAsJsonArray();
                        for (JsonElement dron : dronesArray){
                           this.drones.add(GSON.fromJson(dron,Drone.class));
                        }
                    }
                }
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
            if (json.contains("'isError':0")){
                return true;
            } else {
                return false;
            }
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void updateHangars() {
        String params = "flashAPI/inventory.php?action=getHangarList";
        String decodeString = this.main.backpage.getDataInventory(params);

        if (decodeString != null) {
            JsonArray hangarsArray =  new JsonParser().parse(decodeString).getAsJsonObject().get("data").getAsJsonObject()
                    .get("ret").getAsJsonObject().get("hangars").getAsJsonArray();
            for (JsonElement hangar : hangarsArray) {
                this.hangars.add(GSON.fromJson(hangar,Hangar.class));
            }
        }
    }

    public String getActiveHangar(){
        for(Hangar hangar : hangars){
            if (hangar.hangarIsActive()){
                return hangar.getHangarID();
            }
        }
        return null;
    }
}
