package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.managers.EternalBlacklightGateAPI;
import eu.darkbot.api.managers.EternalGateAPI;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class EternalBlacklightProxy extends Updatable implements EternalBlacklightGateAPI {
    public int cpuCount, currentWave, furthestWave, boosterPoints;
    public boolean isEventEnabled;
    public Leaderboard myRank = new Leaderboard();

    public List<EternalBlacklightProxy.Booster> activeBoosters  = new ArrayList<>();
    public List<EternalBlacklightProxy.Booster> boostersOptions = new ArrayList<>();
    public List<EternalBlacklightProxy.Leaderboard> topRankers  = new ArrayList<>();

    private final ObjArray activeBoostersArr   = ObjArray.ofVector(true);
    private final ObjArray boostersOptionsArr  = ObjArray.ofVector(true);
    private final ObjArray topRankersArr       = ObjArray.ofVector(true);

    @Override
    public void update() {
        if (address == 0) return;

        long data = API.readMemoryLong(address + 48) & ByteUtils.ATOM_MASK;

        this.furthestWave    = API.readMemoryInt(data + 0x40);
        this.boosterPoints   = API.readMemoryInt(data + 0x44);
        this.isEventEnabled  = API.readMemoryBoolean(data + 0x48);
        this.cpuCount        = API.readMemoryInt(API.readMemoryLong(data + 0x68) + 0x28);
        this.currentWave     = API.readMemoryInt(API.readMemoryLong(data + 0x70) + 0x28);

        this.activeBoostersArr.update(API.readMemoryLong( data + 0x78));
        this.boostersOptionsArr.update(API.readMemoryLong(data + 0x80));
        this.topRankersArr.update(API.readMemoryLong(data + 0x90));
        this.myRank.update(API.readMemoryLong(data + 0x98));

        this.activeBoostersArr.sync(activeBoosters, EternalBlacklightProxy.Booster::new, null);
        this.boostersOptionsArr.sync(boostersOptions, EternalBlacklightProxy.Booster::new, null);
        this.topRankersArr.sync(topRankers, EternalBlacklightProxy.Leaderboard::new, null);
    }

    public static class Booster extends UpdatableAuto implements EternalBlacklightGateAPI.Booster {
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

    public static class Leaderboard extends UpdatableAuto {
        public int waves, rank;
        public String lastUpdateTime, name;

        @Override
        public void update() {
            this.waves = API.readMemoryInt(address + 0x20);
            this.rank = API.readMemoryInt(address + 0x24);
            this.lastUpdateTime = API.readMemoryString(address, 0x28);
            this.name = API.readMemoryString(address, 0x30);
        }

        @Override
        public String toString() {
            return "Leaderboard{" +
                    "waves=" + waves +
                    ", rank=" + rank +
                    ", lastUpdateTime=" + lastUpdateTime +
                    ", name=" + name + '\'' +
                    '}';
        }
    }

    @Override
    public int getCpuCount() {
        return cpuCount;
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
    public List<? extends EternalBlacklightGateAPI.Booster> getActiveBoosters() {
        return activeBoosters;
    }

    @Override
    public List<? extends EternalBlacklightGateAPI.Booster> getBoosterOptions() {
        return boostersOptions;
    }
}
