package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import eu.darkbot.api.API;
import eu.darkbot.api.managers.DispatchAPI;
import lombok.Getter;
import lombok.ToString;

@Getter
public class DispatchProxy extends Updatable implements API.Singleton {
    private final FlashList<RewardLoot> rewardLoots = FlashList.ofVector(RewardLoot::new);

    @Override
    public void update() {
        rewardLoots.update(readAtom(0x30, 0x70));
    }

    @Getter
    @ToString
    private static class RewardLoot extends Auto implements DispatchAPI.RewardLoot {
        private String lootId;
        private int amount;

        @Override
        public void update() {
            this.amount = readInt(0x20);
            this.lootId = readString(0x28);
        }
    }

}