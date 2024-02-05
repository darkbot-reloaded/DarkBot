package com.github.manolo8.darkbot.core.objects.gui;

import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.swf.FlashListLong;
import eu.darkbot.api.API;
import eu.darkbot.api.managers.OreAPI;
import lombok.ToString;

import java.util.Arrays;
import java.util.EnumMap;

import static com.github.manolo8.darkbot.Main.API;

public class RefinementGui extends Gui implements API.Singleton {
    private final EnumMap<OreAPI.UpgradeSlot, Upgrade> upgrades = new EnumMap<>(OreAPI.UpgradeSlot.class);
    private final FlashListLong oresArray = FlashListLong.ofArray();

    private final int[] ores;

    public RefinementGui() {
        this.ores = new int[OreAPI.Ore.values().length];
        Arrays.fill(ores, -1);
    }

    public int getAmount(OreAPI.Ore ore) {
        return ores[ore.ordinal()];
    }

    public OreAPI.Upgrade getUpgrade(OreAPI.UpgradeSlot upgradeSlot) {
        return upgrades.get(upgradeSlot);
    }

    @Override
    public void update(long address) {
        if (this.address != address) {
            upgrades.clear();
            Arrays.fill(ores, -1);
        }
        super.update(address);
    }

    @Override
    public void update() {
        super.update();
        if (address == 0) return;
        upgradeOres(37);
        upgradeOres(31);

        oresArray.update(API.readLong(getElementsList(32), 0xB8));

        // upgrade tab
        for (int i = 0; i < oresArray.size(); i++) {
            long addr = oresArray.getLong(i);
            if (API.readInt(addr, 0xA8) != 46) continue;

            int typeId = API.readInt(addr, 0xB0);
            OreAPI.UpgradeSlot upgradeSlot = OreAPI.UpgradeSlot.of(typeId - 1);
            if (upgradeSlot != null) {
                OreAPI.Ore ore = OreAPI.Ore.of(API.readInt(addr, 0x118, 0xB8));
                if (ore != null) {
                    int amount = API.readInt(addr, 0xF0);
                    upgrades.computeIfAbsent(upgradeSlot, u -> new Upgrade())
                            .set(ore, amount);
                }
            }
        }
    }

    private void upgradeOres(int listId) {
        FlashListLong oresArray = this.oresArray;
        oresArray.update(API.readLong(getElementsList(listId), 0xB8));

        for (int i = 0; i < oresArray.size(); i++) {
            long addr = oresArray.getLong(i);
            if (API.readInt(addr, 0xA8) != 46) continue;

            OreAPI.Ore ore = OreAPI.Ore.of(API.readInt(addr, 0xB0));
            if (ore != null)
                ores[ore.ordinal()] = API.readInt(addr, 0xF0);
        }
    }

    @ToString
    protected static class Upgrade implements OreAPI.Upgrade {
        private OreAPI.Ore ore;
        private int amount;

        @Override
        public OreAPI.Ore getOre() {
            return ore;
        }

        @Override
        public int getAmount() {
            return amount;
        }

        void set(OreAPI.Ore ore, int amount) {
            this.ore = ore;
            this.amount = amount;
        }
    }
}
