package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import eu.darkbot.api.API;
import eu.darkbot.api.managers.DispatchAPI;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

@Getter
public class DispatchProxy extends Updatable implements API.Singleton {
    @Getter(AccessLevel.NONE)
    private final ObjArray rewardLootArr = ObjArray.ofVector(true);

    private final List<RewardLoot> rewardLoots = new ArrayList<>();

    @Override
    public void update() {
        rewardLootArr.update(API.readMemoryPtr(address, 0x30, 0x70));
        rewardLootArr.sync(rewardLoots, RewardLoot::new);
    }

    @Getter
    @ToString
    private static class RewardLoot extends Auto implements DispatchAPI.RewardLoot {
        private String lootId;
        private int amount;

        @Override
        public void update() {
            this.amount = API.readMemoryInt(address + 0x20);
            this.lootId = API.readMemoryString(address, 0x28);
        }
    }

}