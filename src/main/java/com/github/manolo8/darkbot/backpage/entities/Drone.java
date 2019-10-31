package com.github.manolo8.darkbot.backpage.entities;

import com.google.gson.annotations.SerializedName;

public class Drone {

    @SerializedName("L")
    private int lootId;

    @SerializedName("repair")
    private int repairPrice;

    @SerializedName("I")
    private String itemId;
    @SerializedName("currency")
    private String repairCurrency;
    @SerializedName("LV")
    private int droneLevel;
    @SerializedName("HP")
    private String damage;
    @SerializedName("SL")
    private int upgradeLevel;

    public Drone(int lootId, int repairPrice, String itemId, String repairCurrency, int droneLevel, String damage, int upgradeLevel) {
        this.lootId = lootId;
        this.repairPrice = repairPrice;
        this.itemId = itemId;
        this.repairCurrency = repairCurrency;
        this.droneLevel = droneLevel;
        this.damage = damage;
        this.upgradeLevel = upgradeLevel;
    }

    public String getLoot() {
        switch (lootId){
            case 1:
                return "drone_flax";
            case 2:
                return "drone_iris";
            case 3:
                return "drone_apis";
            case 4:
                return "drone_zeus";
        }

        return "drone_iris";
    }

    public int getRepairPrice() {
        return repairPrice;
    }

    public String getItemId() {
        return itemId;
    }

    public String getRepairCurrency() {
        return repairCurrency;
    }

    public int getDroneLevel() {
        return droneLevel;
    }

    public int getDamage() {
        return Integer.parseInt(damage.replaceAll("\\D+", ""));
    }
    
    public int getUpgradeLevel(){
        return upgradeLevel;
    }
  
}
