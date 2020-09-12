package com.github.manolo8.darkbot.backpage.hangar;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class Pet extends Item {
    @SerializedName("HP") private String health;
    @SerializedName("PN") private String name;
    private Map<String, Integer> lockedSlots;

    public String getHealth() {
        return health;
    }

    public String getName() {
        return name;
    }

    public Map<String, Integer> getLockedSlots() {
        return lockedSlots;
    }

    @Override
    public String toString() {
        return "Pet{" +
                "health='" + health + '\'' +
                ", name='" + name + '\'' +
                ", lockedSlots=" + lockedSlots +
                "} " + super.toString();
    }
}
