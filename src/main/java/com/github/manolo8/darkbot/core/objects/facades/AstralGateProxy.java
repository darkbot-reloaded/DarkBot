package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import eu.darkbot.api.managers.AstralGateAPI;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;


public class AstralGateProxy extends Updatable implements AstralGateAPI {
    private int highScore, currentRift, currentScore, cpuCount;
    private boolean canEquip;
    private final List<AstralItem> rewardItems = new ArrayList<>();
    private final List<AstralItem> inventoryItems = new ArrayList<>();

    private final ObjArray rewardItemsArr = ObjArray.ofVector(true);
    private final ObjArray inventoryItemsArr = ObjArray.ofVector(true);

    @Override
    public void update() {
        if (address == 0) {
            return;
        }

        long data = API.readMemoryPtr(address + 48);

        this.highScore = API.readMemoryInt(data + 64);
        this.currentRift = API.readMemoryInt(data, 80, 40);
        this.currentScore = API.readMemoryInt(data, 88, 40);
        this.cpuCount = API.readMemoryInt(data, 96, 40);
        this.canEquip = API.readBoolean(data, 0x0B0, 0x20);

        rewardItemsArr.update(API.readMemoryPtr(data + 0x88));
        rewardItemsArr.sync(rewardItems, AstralItem::new);

        inventoryItemsArr.update(API.readMemoryPtr(data + 0x0A0));
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
        private final List<ItemStat> itemStats = new ArrayList<>();
        private final ObjArray itemStatsArr = ObjArray.ofVector(true);

        @Override
        public void update() {
            if (address == 0) {
                return;
            }

            this.equipped = API.readBoolean(address + 0x24);
            long itemData = API.readMemoryPtr(address + 0x30);
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
        public List<ItemStat> getStats() {
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