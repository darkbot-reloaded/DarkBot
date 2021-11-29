package com.github.manolo8.darkbot.backpage.auction;

public class AuctionItems {
    protected String id, lootID, name, type, highestBidder;
    protected double currentBid, ownBid, instantBuy;
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

    public String getHighestBidder(){return highestBidder;}

    public void setHighestBidder(String highest) {
        this.highestBidder = highest;
    }

    public double getCurrentBid(){return currentBid;}

    public void setCurrentBid(double currentBid) {
        this.currentBid = currentBid;
    }

    public double getOwnBid(){return ownBid;}

    public void setOwnBid(double ownBid) {
        this.ownBid = ownBid;
    }

    public double getInstantBuy(){return instantBuy;}

    public void setInstantBuy(double instant) {
        this.instantBuy = instantBuy;
    }

    @Override
    public String toString() {
        return "Auction Item{" +
                "id=" + id +
                "lootID=" + lootID +
                "name=" + name +
                "type=" + type +
                "highestBidder=" + highestBidder +
                "currentBid=" + currentBid +
                "ownBid=" + ownBid +
                "instant=" + instantBuy +
                "}";
    }
}
