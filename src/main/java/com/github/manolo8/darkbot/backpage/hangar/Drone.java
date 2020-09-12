package com.github.manolo8.darkbot.backpage.hangar;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class Drone extends Item {
    @SerializedName("HP") private String health;
    @SerializedName("EF") private String droneEffect;
    @SerializedName("BE") private Map<String, String> droneBonusEffects;
    @SerializedName("DE") private String droneDesignEffects;
    @SerializedName("CU") private String droneDesign;
    @SerializedName("DL") private String damageLevel;
    @SerializedName("SL") private String shieldLevel;
    @SerializedName("repair") private int repairCost;
    @SerializedName("currency") private String currency;

    public String getHealth() {
        return health;
    }

    public String getDroneEffect() {
        return droneEffect;
    }

    public Map<String, String> getDroneBonusEffects() {
        return droneBonusEffects;
    }

    public String getDroneDesignEffects() {
        return droneDesignEffects;
    }

    public String getDroneDesign() {
        return droneDesign;
    }

    public String getDamageLevel() {
        return damageLevel;
    }

    public String getShieldLevel() {
        return shieldLevel;
    }

    public int getRepairCost() {
        return repairCost;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public String toString() {
        return "Drone{" +
                "health='" + health + '\'' +
                ", droneEffect='" + droneEffect + '\'' +
                ", droneBonusEffects=" + droneBonusEffects +
                ", droneDesignEffects='" + droneDesignEffects + '\'' +
                ", droneDesign='" + droneDesign + '\'' +
                ", damageLevel='" + damageLevel + '\'' +
                ", shieldLevel='" + shieldLevel + '\'' +
                ", repairCost=" + repairCost +
                ", currency='" + currency + '\'' +
                "} " + super.toString();
    }
}