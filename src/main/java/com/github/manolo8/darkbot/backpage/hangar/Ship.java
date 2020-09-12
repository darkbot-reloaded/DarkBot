package com.github.manolo8.darkbot.backpage.hangar;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class Ship {
    @SerializedName("I") private int shipId;
    @SerializedName("L") private int lootId;
    @SerializedName("HP") private String health;
    @SerializedName("SM") private String selectedDesign;
    @SerializedName("M") private String[] availableDesigns;
        /*@SerializedName("SCE") // dunno for now
    @SerializedName("SCU")*/

    public int getShipId() {
        return shipId;
    }

    public int getLootId() {
        return lootId;
    }

    public String getHealth() {
        return health;
    }

    public String getSelectedDesign() {
        return selectedDesign;
    }

    public String[] getAvailableDesigns() {
        return availableDesigns;
    }

    @Override
    public String toString() {
        return "Ship{" +
                "shipId=" + shipId +
                ", lootId=" + lootId +
                ", health='" + health + '\'' +
                ", selectedDesign='" + selectedDesign + '\'' +
                ", availableDesigns=" + Arrays.toString(availableDesigns) +
                '}';
    }
}