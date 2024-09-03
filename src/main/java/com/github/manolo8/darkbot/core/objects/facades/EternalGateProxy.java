package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.managers.EternalGateAPI;

import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class EternalGateProxy extends Updatable implements EternalGateAPI {
    public int keys, boosterPoints, currentWave, furthestWave;

    public FlashList<Booster> activeBoosters  = FlashList.ofVector(Booster::new);
    public FlashList<Booster> boostersOptions = FlashList.ofVector(Booster::new);

    @Override
    public void update() {
        if (address == 0) return;

        long data = API.readLong(address + 48) & ByteUtils.ATOM_MASK;

        this.currentWave   = API.readInt(data + 0x40);
        this.furthestWave  = API.readInt(data + 0x44);
        this.keys          = API.readInt(API.readLong(data + 0x58) + 0x28);
        this.boosterPoints = API.readInt(API.readLong(data + 0x60) + 0x28);

        this.activeBoosters.update(API.readLong( data + 0x68));
        this.boostersOptions.update(API.readLong(data + 0x70));
    }

    public static class Booster extends Auto implements EternalGateAPI.Booster {
        public int percentage;
        public String category;

        @Override
        public void update() {
            this.percentage = API.readInt(address + 0x20);
            this.category   = API.readString(API.readLong(address + 0x28));
        }

        @Override
        public int getPercentage() {
            return percentage;
        }

        @Override
        public String getCategory() {
            return category;
        }
    }

    @Override
    public int getKeys() {
        return keys;
    }

    @Override
    public int getBoosterPoints() {
        return boosterPoints;
    }

    @Override
    public int getCurrentWave() {
        return currentWave;
    }

    @Override
    public int getFurthestWave() {
        return furthestWave;
    }

    @Override
    public List<? extends EternalGateAPI.Booster> getActiveBoosters() {
        return activeBoosters;
    }

    @Override
    public List<? extends EternalGateAPI.Booster> getBoosterOptions() {
        return boostersOptions;
    }
}