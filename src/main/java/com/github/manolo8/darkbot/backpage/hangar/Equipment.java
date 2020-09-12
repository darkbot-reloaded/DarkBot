package com.github.manolo8.darkbot.backpage.hangar;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class Equipment {
    @SerializedName("EQ")
    private Map<String, int[]> equipped;

    public int[] getEquippedOfType(String type) {
        return getEquipped().get(type);
    }

    public Map<String, int[]> getEquipped() {
        return equipped;
    }

    @Override
    public String toString() {
        return "Equipment{" +
                "equipped=" + equipped +
                '}';
    }
}
