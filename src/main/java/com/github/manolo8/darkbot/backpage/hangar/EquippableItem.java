package com.github.manolo8.darkbot.backpage.hangar;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class EquippableItem extends Item {
    private Map<String, String> properties;
    @SerializedName("Q") private Integer quantity;

    public Map<String, String> getProperties() {
        return properties;
    }

    public Integer getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "EquippableItem{" +
                "properties=" + properties +
                ", quantity=" + quantity +
                "} " + super.toString();
    }
}
