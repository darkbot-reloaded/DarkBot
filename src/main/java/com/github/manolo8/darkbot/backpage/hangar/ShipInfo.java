package com.github.manolo8.darkbot.backpage.hangar;

import com.google.gson.annotations.SerializedName;

public class ShipInfo {
    private String lootId;
    private int owned;
    @SerializedName("fav") private int favourite;
    private Price price;
    private int hangarId;
    private int factionBased;
    private int eventItem;
    private int eventActive;

    public String getLootId() {
        return lootId;
    }

    public int getOwned() {
        return owned;
    }

    public int getFavourite() {
        return favourite;
    }

    public Price getPrice() {
        return price;
    }

    public int getHangarId() {
        return hangarId;
    }

    public int getFactionBased() {
        return factionBased;
    }

    public int getEventItem() {
        return eventItem;
    }

    public int getEventActive() {
        return eventActive;
    }

    @Override
    public String toString() {
        return "ShipInfo{" +
                "lootId='" + lootId + '\'' +
                ", owned=" + owned +
                ", favourite=" + favourite +
                ", price=" + price +
                ", hangarId=" + hangarId +
                ", factionBased=" + factionBased +
                ", eventItem=" + eventItem +
                ", eventActive=" + eventActive +
                '}';
    }

    public static class Price {
        private int uridium, credits, isPayment;

        public int getUridium() {
            return uridium;
        }

        public int getCredits() {
            return credits;
        }

        public int getIsPayment() {
            return isPayment;
        }

        @Override
        public String toString() {
            return "Price{" +
                    "uridium=" + uridium +
                    ", credits=" + credits +
                    ", isPayment=" + isPayment +
                    '}';
        }
    }
}
