package com.github.manolo8.darkbot.backpage.hangar;

import com.google.gson.annotations.SerializedName;

public abstract class Item {
    @SerializedName("L") protected int lootId;
    @SerializedName("LV") protected Integer level;
    @SerializedName("I") protected String itemId;

    public int getLootId() {
        return lootId;
    }

    public Integer getLevel() {
        return level;
    }

    public String getItemId() {
        return itemId;
    }

    @Override
    public String toString() {
        return "Item{" +
                "lootId=" + lootId +
                ", level=" + level +
                ", itemId='" + itemId + '\'' +
                '}';
    }
}
