package com.github.manolo8.darkbot.backpage.auction;

import com.github.manolo8.darkbot.config.actions.conditions.HasEffectCondition;

public class AuctionItems {

    protected enum auctionTypes{
        HOUR,
        DAY,
        WEEK;
    }; //0=hour, 1=day, 2=hour
    public auctionTypes auctionType;
    protected String id, lootID, name, itemType;
    protected long highestBidderID, currentBid, ownBid, instantBuy;

    public auctionTypes getAuctionType(){
        return auctionType;
    }

    public void setAuctionType(auctionTypes type){
        auctionType = type;
    }

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

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
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
                "auctionType=" + auctionType +
                "id=" + id +
                "lootID=" + lootID +
                "name=" + name +
                "itemType=" + itemType +
                "highestBidder=" + highestBidderID +
                "currentBid=" + currentBid +
                "ownBid=" + ownBid +
                "instant=" + instantBuy +
                "}";
    }
}
