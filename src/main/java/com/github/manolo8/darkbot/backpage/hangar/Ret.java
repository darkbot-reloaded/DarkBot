package com.github.manolo8.darkbot.backpage.hangar;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class Ret {
    private Map<String, int[]> filters;
    @JsonAdapter(value = Hangar.HangarAdapter.class) private List<Hangar> hangars;
    @SerializedName("items") private List<EquippableItem> items;
    @SerializedName("itemInfo") private List<ItemInfo> itemInfos;
    @SerializedName("shipInfo") private List<ShipInfo> shipInfos;

    public int[] getTypeFilters(FilterType type) {
        return getFilters().get(type.toString());
    }

    public Map<String, int[]> getFilters() {
        return filters;
    }

    public List<Hangar> getHangars() {
        return hangars;
    }

    public List<EquippableItem> getItems() {
        return items;
    }

    public List<ItemInfo> getItemInfos() {
        return itemInfos;
    }

    public List<ShipInfo> getShipInfos() {
        return shipInfos;
    }

    @Override
    public String toString() {
        return "Ret{" +
                "filters=" + filters +
                ", hangars=" + hangars +
                ", items=" + items +
                ", itemInfos=" + itemInfos +
                ", shipInfos=" + shipInfos +
                '}';
    }

    public enum FilterType {
        AMMUNITION,
        DRONE_RELATED,
        EXTRAS,
        GENERATORS,
        MODULES,
        PET_RELATED,
        RESOURCES,
        SHIP_UPGRADES,
        WEAPONS;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
