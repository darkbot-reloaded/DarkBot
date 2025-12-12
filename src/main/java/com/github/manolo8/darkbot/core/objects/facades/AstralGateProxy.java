package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import eu.darkbot.api.managers.AstralGateAPI;

import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class AstralGateProxy extends AbstractProxy implements AstralGateAPI {
    private int highScore, currentRift, currentScore, cpuCount;
    private boolean canEquip;
    public final FlashList<AstralItem> rewardItems = FlashList.ofVector(AstralItem::new);
    public final FlashList<AstralItem> inventoryItems = FlashList.ofVector(AstralItem::new);

    @Override
    public void updateProxy() {
        if (address == 0) return;

        this.highScore = readInt(0x40);
        this.currentRift = readBindableInt(0x50);
        this.currentScore = readBindableInt(0x58);
        this.cpuCount = readBindableInt(0x60);
        this.canEquip = readBoolean(0xb0, 0x20);

        rewardItems.update(readAtom(0x88));
        inventoryItems.update(readAtom(0xa0));
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