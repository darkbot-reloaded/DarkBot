package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.api.managers.EternalBlacklightGateAPI;
import eu.darkbot.util.Timer;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

@ApiStatus.Internal
public class EternalBlacklightProxy extends Updatable implements EternalBlacklightGateAPI {
    private final Timer boosterClickTimer = Timer.get(500);

    private final Main main;

    public FlashList<EternalBlacklightProxy.Booster> activeBoosters  = FlashList.ofVector(Booster::new);
    public FlashList<EternalBlacklightProxy.Booster> boostersOptions = FlashList.ofVector(Booster::new);
    public FlashList<EternalBlacklightProxy.Leaderboard> topRankers  = FlashList.ofVector(Leaderboard::new);

    public Leaderboard myRank = new Leaderboard();

    @Getter
    public int cpuCount, currentWave, furthestWave, boosterPoints;
    public boolean isEventEnabled;

    private boolean clickedTab;

    public EternalBlacklightProxy(Main main) {
        this.main = main;
    }

    @Override
    public void update() {
        if (address == 0) return;

        long data = API.readLong(address + 48) & ByteUtils.ATOM_MASK;

        this.furthestWave    = API.readInt(data + 0x40);
        this.boosterPoints   = API.readInt(data + 0x44);
        this.isEventEnabled  = API.readBoolean(data + 0x48);
        this.cpuCount        = API.readInt(API.readLong(data + 0x68) + 0x28);
        this.currentWave     = API.readInt(API.readLong(data + 0x70) + 0x28);

        this.activeBoosters.update(API.readLong( data + 0x78));
        this.boostersOptions.update(API.readLong(data + 0x80));
        this.topRankers.update(API.readLong(data + 0x90));
        this.myRank.update(API.readLong(data + 0x98));
    }

    @Override
    public boolean isEventEnabled() {
        return isEventEnabled;
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

            int i;
            if (!(booster instanceof EternalBlacklightProxy.Booster) || (i = boostersOptions.indexOf(booster)) == -1)
                throw new IllegalArgumentException("Booster argument have to be value from #getBoosterOptions() list!");

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

    @ApiStatus.Internal
    @Getter
    @ToString
    public static class Booster extends Auto implements EternalBlacklightGateAPI.Booster {
        public int percentage;
        public String category;
        private Category categoryType;

        @Override
        public void update() {
            this.percentage = API.readInt(address + 0x20);

            String category = API.readString(API.readLong(address + 0x28));
            if (!category.equals(this.category)) {
                categoryType = Category.of(category);
            }
            this.category = category;
        }
    }

    @ApiStatus.Internal
    @Getter
    @ToString
    public static class Leaderboard extends Auto implements UserRank {
        public int waves, rank;
        public String lastUpdateTime, name;

        @Override
        public void update() {
            this.waves = API.readInt(address + 0x20);
            this.rank = API.readInt(address + 0x24);
            this.lastUpdateTime = API.readString(address, 0x28);
            this.name = API.readString(address, 0x30);
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
