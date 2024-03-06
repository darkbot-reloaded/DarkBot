package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import eu.darkbot.api.managers.AstralGateAPI;

import java.util.List;

import static com.github.manolo8.darkbot.Main.API;


public class AstralGateProxy extends Updatable implements AstralGateAPI {
    private int highScore, currentRift, currentScore, cpuCount;
    private boolean canEquip;
    public final FlashList<AstralItem> rewardItems = FlashList.ofVector(AstralItem::new);
    public final FlashList<AstralItem> inventoryItems = FlashList.ofVector(AstralItem::new);

    @Override
    public void update() {
        if (address == 0) {
            return;
        }

        long data = API.readAtom(address + 48);

        this.highScore = API.readInt(data + 64);
        this.currentRift = API.readInt(data, 80, 40);
        this.currentScore = API.readInt(data, 88, 40);
        this.cpuCount = API.readInt(data, 96, 40);
        this.canEquip = API.readBoolean(data, 0x0B0, 0x20);

        rewardItems.update(API.readAtom(data + 0x88));
        inventoryItems.update(API.readAtom(data + 0x0A0));
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
        private final FlashList<ItemStat> itemStats = FlashList.ofVector(ItemStat::new);

        @Override
        public void update() {
            if (address == 0) {
                return;
            }

            this.equipped = API.readBoolean(address + 0x24);
            long itemData = API.readAtom(address + 0x30);
            this.upgradeLevel = API.readInt(itemData + 0x2C);

            this.lootId = API.readString(itemData, 0x48);

            itemStats.update(API.readLong(address + 0x38));
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