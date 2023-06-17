package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.managers.DispatchAPI;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class DispatchProxy extends Updatable implements DispatchAPI {

    public List<RewardsLoot> popupMessageLoots = new ArrayList<>();
    private final ObjArray popupMessageLootArr = ObjArray.ofVector(true);

    @Override
    public void update() {
        long data = API.readMemoryLong(address + 0x30) & ByteUtils.ATOM_MASK;
        popupMessageLootArr.update(API.readMemoryLong(data + 0x70) & ByteUtils.ATOM_MASK);
        popupMessageLootArr.sync(popupMessageLoots, RewardsLoot::new);
    }

    @Override
    public List<? extends RewardLoot> getRewardLoot() {
        return popupMessageLoots;
    }

    public static class RewardsLoot extends Auto implements RewardLoot {
        public String lootId;
        public int amount;

        public void update() {
            if (address <= 0) return;
            this.amount = API.readMemoryInt(address + 0x20);
            this.lootId = API.readMemoryString(address, 0x28);
        }

        @Override
        public String getLootId() {
            return lootId;
        }

        @Override
        public int getAmount() {
            return amount;
        }
    }
}