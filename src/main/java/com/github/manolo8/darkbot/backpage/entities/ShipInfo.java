package com.github.manolo8.darkbot.backpage.entities;

import com.google.gson.annotations.SerializedName;

public class ShipInfo {

    @SerializedName("lootId")
    private String lootId;

    @SerializedName("owned")
    private Integer owned;

    @SerializedName("fav")
    private Integer fav;

    @SerializedName("hangarId")
    private String hangarId;

    @SerializedName("factionBased")
    private Integer factionBased;

    @SerializedName("eventItem")
    private Integer eventItem;

    @SerializedName("eventActive")
    private Integer eventActive;

    public ShipInfo(String lootId, Integer owned, Integer fav, String hangarId, Integer factionBased, Integer eventItem, Integer eventActive) {
        this.lootId = lootId;
        this.owned = owned;
        this.fav = fav;
        this.hangarId = hangarId;
        this.factionBased = factionBased;
        this.eventItem = eventItem;
        this.eventActive = eventActive;
    }

    public String getLootId() {
        return lootId;
    }

    public Integer getOwned() {
        return owned;
    }

    public Integer getFav() {
        return fav;
    }

    public String getHangarId() {
        return hangarId;
    }

    public Integer getFactionBased() {
        return factionBased;
    }

    public Integer getEventItem() {
        return eventItem;
    }

    public Integer getEventActive() {
        return eventActive;
    }

}
