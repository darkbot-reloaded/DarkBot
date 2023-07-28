package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import eu.darkbot.api.managers.AstralGateAPI;

import static com.github.manolo8.darkbot.Main.API;

import java.util.ArrayList;
import java.util.List;

public class AstralGateProxy extends Updatable implements AstralGateAPI {
    private int highScore, currentRift, currentScore, cpuCount;
    private boolean canEquip;
    private List<AstralItem> rewardItems = new ArrayList<>();
    private List<AstralItem> inventoryItems = new ArrayList<>();

    private final ObjArray rewardItemsArr = ObjArray.ofVector(true);
    private final ObjArray inventoryItemsArr = ObjArray.ofVector(true);

    @Override
    public void update() {
        if (address == 0) {
            return;
        }

        long data = API.readMemoryLong(address + 48) & ByteUtils.ATOM_MASK;

        this.highScore = API.readMemoryInt(data + 64);
        this.currentRift = API.readMemoryInt(API.readMemoryLong(data + 80) + 40);
        this.currentScore = API.readMemoryInt(API.readMemoryLong(data + 88) + 40);
        this.cpuCount = API.readMemoryInt(API.readMemoryLong(data + 96) + 40);
        this.canEquip = API.readBoolean(API.readMemoryLong(data + 0x0B0) + 0x20);

        rewardItemsArr.update(API.readMemoryLong(data + 0x88));
        rewardItemsArr.sync(rewardItems, AstralItem::new);

        inventoryItemsArr.update(API.readMemoryLong(data + 0x0A0));
        inventoryItemsArr.sync(inventoryItems, AstralItem::new);
    }

    @Override
    public List<AstralItem> getRewardsItems() {
        return rewardItems;
    }

    @Override
    public List<AstralItem> getInventoryItems() {
        return inventoryItems;
    }

    @Override
    public boolean allowedToEquip() {
        return canEquip;
    }

    @Override
    public int getHighScore() {
        return highScore;
    }

    @Override
    public int getCurrentRift() {
        return currentRift;
    }

    @Override
    public int getCurrentScore() {
        return currentScore;
    }

    @Override
    public int getCpuCount() {
        return cpuCount;
    }

    public static class AstralItem extends Auto implements AstralGateAPI.AstralItem {
        private boolean equipped;
        private int upgradeLevel;
        private String lootId;
        private List<ItemStat> itemStats = new ArrayList<>();
        private final ObjArray itemStatsArr = ObjArray.ofVector(true);

        @Override
        public void update() {
            if (address == 0) {
                return;
            }

            this.equipped = API.readBoolean(address + 0x24);
            long itemData = API.readMemoryLong(address + 0x30) & ByteUtils.ATOM_MASK;
            this.upgradeLevel = API.readInt(itemData + 0x2C);

            this.lootId = API.readString(itemData, 0x48);

            itemStatsArr.update(API.readMemoryLong(address + 0x38));
            itemStatsArr.sync(itemStats, ItemStat::new);
        }

        @Override
        public boolean isEquipped() {
            return equipped;
        }

        @Override
        public int getUpgradeLevel() {
            return upgradeLevel;
        }

        @Override
        public String getLootId() {
            return lootId;
        }

        @Override
        public List<ItemStat> getTheStatsList() {
            return itemStats;
        }
    }

    public static class ItemStat extends Auto implements AstralGateAPI.ItemStat {
        private String attribute;
        private double value;

        @Override
        public void update() {
            if (address == 0) {
                return;
            }

            this.attribute = API.readString(address, 0x20);
            this.value = API.readDouble(address + 0x28);
        }

        @Override
        public String getAttribute() {
            return attribute;
        }

        @Override
        public double getValue() {
            return value;
        }
    }
}