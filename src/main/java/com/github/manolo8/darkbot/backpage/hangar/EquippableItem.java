package com.github.manolo8.darkbot.backpage.hangar;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class EquippableItem extends Item {
    private Map<String, String> properties;
    @SerializedName("Q") private Integer quantity;

    @SerializedName("EQH") private String equippedHangar;
    @SerializedName("EQC") private String equippedConfig;
    @SerializedName("EQT") private String equippedTarget;

    @SerializedName("SL") private int shieldLevel;
    @SerializedName("DL") private int damageLevel;

    @SerializedName("SUS") private String shipUpgradeShips;
    @SerializedName("SUM") private String shipUpgradeModifiers;

    public Map<String, String> getProperties() {
        return properties;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getEquippedHangar() {
        return equippedHangar;
    }

    public String getEquippedConfig() {
        return equippedConfig;
    }

    public String getEquippedTarget() {
        return equippedTarget;
    }

    public int getShieldLevel() {
        return shieldLevel;
    }

    public int getDamageLevel() {
        return damageLevel;
    }

    public String getShipUpgradeShips() {
        return shipUpgradeShips;
    }

    public String getShipUpgradeModifiers() {
        return shipUpgradeModifiers;
    }

    @Override
    public String toString() {
        return "EquippableItem{" +
                "properties=" + properties +
                ", quantity=" + quantity +
                "} " + super.toString();
    }
}
