package com.github.manolo8.darkbot.backpage.auction;

public class AuctionItems {
    protected String id, lootID, name, type;
    protected long highestBidderID, currentBid, ownBid, instantBuy;
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

    public long getHighestBidder(){return highestBidderID;}

    public void setHighestBidderID(long highestBidderID) {
        this.highestBidderID = highestBidderID;
    }

    public long getCurrentBid(){return currentBid;}

    public void setCurrentBid(long currentBid) {
        this.currentBid = currentBid;
    }

    public long getOwnBid(){return ownBid;}

    public void setOwnBid(long ownBid) {
        this.ownBid = ownBid;
    }

    public long getInstantBuy(){return instantBuy;}

    public void setInstantBuy(long instantBuy) {
        this.instantBuy = instantBuy;
    }

    @Override
    public String toString() {
        return "Auction Item{" +
                "id=" + id +
                "lootID=" + lootID +
                "name=" + name +
                "type=" + type +
                "highestBidder=" + highestBidderID +
                "currentBid=" + currentBid +
                "ownBid=" + ownBid +
                "instant=" + instantBuy +
                "}";
    }
}
