package com.github.manolo8.darkbot.backpage.auction;

public class AuctionItems {
    protected String id, lootID, name, type, highest, current, you, instantBuy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLootID() {
        return lootID;
    }

    public void setLootID(String id) {
        this.lootID = lootID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHighest(){return highest;}

    public void setHighest(String highest) {
        this.highest = highest;
    }

    public String getCurrent(){return current;}

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getYou(){return you;}

    public void setYou(String you) {
        this.you = you;
    }

    public String getInstantBuy(){return instantBuy;}

    public void setInstantBuy(String instant) {
        this.instantBuy = instantBuy;
    }

    @Override
    public String toString() {
        return "Auction Item{" +
                "id=" + id +
                "lootID=" + lootID +
                "name=" + name +
                "type=" + type +
                "highest=" + highest +
                "current=" + current +
                "you=" + you +
                "instant=" + instantBuy +
                "}";
    }
}
