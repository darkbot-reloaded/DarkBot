package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.Main.UPDATE_LOCKER;

import java.util.ArrayList;
import java.util.List;

public class AstralGateProxy extends Updatable {
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
        synchronized (UPDATE_LOCKER) {
            rewardItemsArr.sync(rewardItems, AstralItem::new);
        }

        inventoryItemsArr.update(API.readMemoryLong(data + 0x0A0));
        synchronized (UPDATE_LOCKER) {
            inventoryItemsArr.sync(inventoryItems, AstralItem::new);
        }
    }

    public List<AstralItem> getRewardsItems() {
        return rewardItems;
    }

    public List<AstralItem> getInventoryItems() {
        return inventoryItems;
    }

    public boolean allowedToEquip() {
        return canEquip;
    }

    public int getHighScore() {
        return highScore;
    }

    public int getCurrentRift() {
        return currentRift;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public static class AstralItem extends Auto {
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
            synchronized (UPDATE_LOCKER) {
                itemStatsArr.sync(itemStats, ItemStat::new);
            }
        }

        public boolean isEquipped() {
            return equipped;
        }

        public int getUpgradeLevel() {
            return upgradeLevel;
        }

        public String getLootId() {
            return lootId;
        }

        public List<ItemStat> getTheStatsList() {
            return itemStats;
        }
    }

    public static class ItemStat extends Auto {
        private String attribute;
        private Double value;

        @Override
        public void update() {
            if (address == 0) {
                return;
            }

            this.attribute = API.readString(address, 0x20);
            this.value = API.readDouble(address + 0x28);
        }

        public String getAttribute() {
            return attribute;
        }

        public Double getValue() {
            return value;
        }
    }
}