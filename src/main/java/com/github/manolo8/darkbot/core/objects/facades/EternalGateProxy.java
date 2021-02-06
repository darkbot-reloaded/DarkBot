package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.managers.EternalGateAPI;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class EternalGateProxy extends Updatable implements EternalGateAPI {
    public int keys, boosterPoints, currentWave, furthestWave;

    public List<Booster> activeBoosters  = new ArrayList<>();
    public List<Booster> boostersOptions = new ArrayList<>();

    private final ObjArray activeBoostersArr   = ObjArray.ofVector(true);
    private final ObjArray boostersOptionsArr  = ObjArray.ofVector(true);

    @Override
    public void update() {
        if (address == 0) return;

        long data = API.readMemoryLong(address + 48) & ByteUtils.ATOM_MASK;

        this.currentWave   = API.readMemoryInt(data + 0x40);
        this.furthestWave  = API.readMemoryInt(data + 0x44);
        this.keys          = API.readMemoryInt(API.readMemoryLong(data + 0x58) + 0x28);
        this.boosterPoints = API.readMemoryInt(API.readMemoryLong(data + 0x60) + 0x28);

        this.activeBoostersArr.update(API.readMemoryLong( data + 0x68));
        this.boostersOptionsArr.update(API.readMemoryLong(data + 0x70));

        this.activeBoostersArr.sync(activeBoosters, Booster::new, null);
        this.boostersOptionsArr.sync(boostersOptions, Booster::new, null);
    }

    public static class Booster extends UpdatableAuto implements EternalGateAPI.Booster {
        public int percentage;
        public String category;

        @Override
        public void update() {
            this.percentage = API.readMemoryInt(address + 0x20);
            this.category   = API.readMemoryString(API.readMemoryLong(address + 0x28));
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