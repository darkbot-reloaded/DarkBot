package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.managers.EternalBlacklightGateAPI;
import eu.darkbot.util.Timer;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class EternalBlacklightProxy extends Updatable implements EternalBlacklightGateAPI {
    private final ObjArray activeBoostersArr = ObjArray.ofVector(true);
    private final ObjArray boostersOptionsArr = ObjArray.ofVector(true);
    private final ObjArray topRankersArr = ObjArray.ofVector(true);
    private final Timer boosterClickTimer = Timer.get(500);

    private final Main main;

    public List<EternalBlacklightProxy.Booster> activeBoosters  = new ArrayList<>();
    public List<EternalBlacklightProxy.Booster> boostersOptions = new ArrayList<>();
    public List<EternalBlacklightProxy.Leaderboard> topRankers  = new ArrayList<>();

    public Leaderboard myRank = new Leaderboard();

    public int cpuCount, currentWave, furthestWave, boosterPoints;
    public boolean isEventEnabled;

    private boolean clickedTab;

    public EternalBlacklightProxy(Main main) {
        this.main = main;
    }

    @Override
    public void update() {
        if (address == 0) return;

        long data = API.readMemoryLong(address + 48) & ByteUtils.ATOM_MASK;

        this.furthestWave    = API.readMemoryInt(data + 0x40);
        this.boosterPoints   = API.readMemoryInt(data + 0x44);
        this.isEventEnabled  = API.readMemoryBoolean(data + 0x48);
        this.cpuCount        = API.readMemoryInt(API.readMemoryLong(data + 0x68) + 0x28);
        this.currentWave     = API.readMemoryInt(API.readMemoryLong(data + 0x70) + 0x28);

        this.activeBoostersArr.update(API.readMemoryLong(data + 0x78));
        this.boostersOptionsArr.update(API.readMemoryLong(data + 0x80));
        this.topRankersArr.update(API.readMemoryLong(data + 0x90));
        this.myRank.update(API.readMemoryLong(data + 0x98));

        this.activeBoostersArr.sync(activeBoosters, EternalBlacklightProxy.Booster::new);
        this.boostersOptionsArr.sync(boostersOptions, EternalBlacklightProxy.Booster::new);
        this.topRankersArr.sync(topRankers, EternalBlacklightProxy.Leaderboard::new);
    }

    @Override
    public boolean isEventEnabled() {
        return isEventEnabled;
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
    public boolean selectBooster(EternalBlacklightGateAPI.Booster booster) {
        Gui blacklightGate = main.guiManager.blacklightGate;
        if (getBoosterPoints() <= 0 || boostersOptions.size() < 3) {
            blacklightGate.hide();
            return false;
        }

        if (boosterClickTimer.tryActivate() && blacklightGate.show(true)) {
            if (!clickedTab) {
                blacklightGate.click(210, 32);
                return clickedTab = true;
            }

            int i = boostersOptions.indexOf(booster);
            if (i == -1) throw new IllegalArgumentException("Booster argument have to be value from #getBoosterOptions() list!");

            clickedTab = false;
            blacklightGate.click(15 + (110 * (i + 1)), 215);
            boosterClickTimer.activate(1000);
        }
        return true;
    }

    @Override
    public List<? extends EternalBlacklightGateAPI.Booster> getActiveBoosters() {
        return activeBoosters;
    }

    @Override
    public List<? extends EternalBlacklightGateAPI.Booster> getBoosterOptions() {
        return boostersOptions;
    }

    @Override
    public UserRank getOwnRank() {
        return myRank;
    }

    @Override
    public List<? extends UserRank> getLeaderboard() {
        return topRankers;
    }

    public static class Booster extends Auto implements EternalBlacklightGateAPI.Booster {
        public int percentage;
        public String category;

        private Category categoryType;

        @Override
        public void update() {
            this.percentage = API.readMemoryInt(address + 0x20);

            String category = API.readMemoryString(API.readMemoryLong(address + 0x28));
            if (!category.equals(this.category)) {
                categoryType = Category.of(category);
            }
            this.category = category;
        }

        @Override
        public int getPercentage() {
            return percentage;
        }

        @Override
        public String getCategory() {
            return category;
        }

        @Override
        public Category getCategoryType() {
            return categoryType;
        }
    }

    public static class Leaderboard extends Auto implements UserRank {
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

        @Override
        public int getRank() {
            return rank;
        }

        @Override
        public int getWave() {
            return waves;
        }

        @Override
        public String getUsername() {
            return name;
        }

        @Override
        public String getUpdateTimeText() {
            return lastUpdateTime;
        }
    }
}
