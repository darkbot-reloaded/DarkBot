package com.github.manolo8.darkbot.backpage.entities;

import com.google.gson.annotations.SerializedName;

public class ItemInfo {

    @SerializedName("L")
    private int lootId;

    @SerializedName("name")
    private String name;

    public ItemInfo(int lootId, String name) {
        this.lootId = lootId;
        this.name = name;
    }

    public int getLoot() {
        return this.lootId;
    }

    public String getName() {
        return this.name;
    }

}