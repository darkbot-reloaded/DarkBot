package com.github.manolo8.darkbot.backpage.auction;

public class AuctionItems {

    public AuctionType auctionType;
    protected String id, lootId, name, itemType;
    protected long highestBidderId, currentBid, ownBid, instantBuy;

    public AuctionType getAuctionType() {
        return auctionType;
    }

    public void setAuctionType(AuctionType type) {
        auctionType = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLootId() {
        return lootId;
    }

    public void setLootId(String lootId) {
        this.lootId = lootId;
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

    public long getHighestBidder() {
        return highestBidderId;
    }

    public void setHighestBidderId(long highestBidderId) {
        this.highestBidderId = highestBidderId;
    }

    public long getCurrentBid() {
        return currentBid;
    }

    public void setCurrentBid(long currentBid) {
        this.currentBid = currentBid;
    }

    public long getOwnBid() {
        return ownBid;
    }

    public void setOwnBid(long ownBid) {
        this.ownBid = ownBid;
    }

    public long getInstantBuy() {
        return instantBuy;
    }

    public void setInstantBuy(long instantBuy) {
        this.instantBuy = instantBuy;
    }

    @Override
    public String toString() {
        return "Auction Item{" +
                "auctionType=" + auctionType +
                "id=" + id +
                "lootId=" + lootId +
                "name=" + name +
                "itemType=" + itemType +
                "highestBidderId=" + highestBidderId +
                "currentBid=" + currentBid +
                "ownBid=" + ownBid +
                "instant=" + instantBuy +
                "}";
    }

    protected enum AuctionType {
        HOUR,
        DAY,
        WEEK
    }
}
