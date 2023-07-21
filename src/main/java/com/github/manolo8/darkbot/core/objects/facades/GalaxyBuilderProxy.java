package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import eu.darkbot.api.game.galaxy.GalaxyGate;
import eu.darkbot.api.game.galaxy.GalaxyInfo;
import eu.darkbot.api.game.galaxy.GateInfo;
import eu.darkbot.api.game.galaxy.SpinResult;
import eu.darkbot.api.managers.GalaxySpinnerAPI;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class GalaxyBuilderProxy extends Updatable implements GalaxySpinnerAPI {

    @Getter
    private final BuilderData galaxyInfo = new BuilderData();
    private int spinsUsed;

    @Override
    public void update() {
        this.galaxyInfo.update(readAtom(48));
    }

    @Override
    public @Nullable Boolean updateGalaxyInfos(int expiryTime) {
        // TODO: implement
        return true;
    }

    @Override
    public Optional<SpinResult> spinGate(@NotNull GalaxyGate gate, boolean multiplier, int spinAmount, int minWait) {
        if (setGate(gate)) {
            Main.API.callMethodChecked(false, "23(267)1016231600", 70, galaxyInfo.address, spinAmount); // set spin amount
            Main.API.callMethodChecked(false, "23(26)008421700", 68, galaxyInfo.address); // make a spin
            spinsUsed += spinAmount;
        }
        // TODO: implement
        return Optional.empty();
    }

    @Override
    public int getSpinsUsed() {
        return spinsUsed;
    }

    @Override
    public boolean placeGate(@NotNull GalaxyGate gate, int minWait) {
        if (setGate(gate)) {
            return Main.API.callMethodChecked(false, "23(26)008411600", 80, galaxyInfo.address);
        }
        return false;
    }

    @Override
    public boolean buyLife(@NotNull GalaxyGate gate, int minWait) {
        if (setGate(gate)) {
            return Main.API.callMethodChecked(false, "23(26)008411600", 62, galaxyInfo.address);
        }
        return false;
    }

    private boolean setGate(GalaxyGate gate) {
        if (!galaxyInfo.initialized) return false;
        return Main.API.callMethodChecked(false, "23(267)1016241700", 19, galaxyInfo.address, gate.getId());
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private static class BuilderData extends Updatable.Auto implements GalaxyInfo {
        private final ObjArray gatesArr = ObjArray.ofArrObj(true);
        private final Map<GalaxyGate, GateInfoImpl> gateData = new EnumMap<>(GalaxyGate.class);
        private int freeEnergy, selectedSpinAmount, energyCost, selectedGateId;
        private boolean initialized;
        private boolean spinSale, galaxyGateDay, bonusRewardsDay;

        private int uridium;

        public BuilderData() {
            for (GalaxyGate gate : GalaxyGate.values()) {
                gateData.put(gate, new GateInfoImpl());
            }
        }

        @Override
        public void update() {
            this.freeEnergy = readInt(64);
            this.energyCost = readInt(68);
            this.selectedGateId = readInt(76);

            this.initialized = readBoolean(80);
            this.selectedSpinAmount = readInt(96);

            this.uridium = (int) readDouble(288);

            if (initialized) {
                this.gatesArr.update(readLong(112));
                for (GalaxyGate gate : GalaxyGate.values()) {
                    gateData.get(gate).update(gatesArr.getPtr(gate.getId() - 1));
                }
            }

            // not sure about order -- need test
            long classClosure = getClassClosure();
            this.spinSale = Main.API.readBoolean(classClosure + 216);
            this.galaxyGateDay = Main.API.readBoolean(classClosure + 220);
            this.bonusRewardsDay = Main.API.readBoolean(classClosure + 224);
        }

        @Override
        public int getSpinSalePercentage() {
            return 100 - energyCost;
        }

        @Override
        public boolean isSpinSale() {
            return spinSale;
        }

        @Override
        public boolean isGalaxyGateDay() {
            return galaxyGateDay;
        }

        @Override
        public boolean isBonusRewardsDay() {
            return bonusRewardsDay;
        }

        @Override
        public Map<GalaxyGate, ? extends GateInfo> getGateInfos() {
            return gateData;
        }

        @Override
        public GateInfo getGateInfo(GalaxyGate galaxyGate) {
            return gateData.get(galaxyGate);
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private static class GateInfoImpl extends Updatable.Auto implements GateInfo {
        private final BonusRewardImpl bonusReward = new BonusRewardImpl();
        private int currentParts, totalParts;
        private int livesLeft, lifePrice;
        private int currentWave, totalWave;
        private boolean readyToPlace;

        @Override
        public void update() {
            if (address == 0) return;
            this.currentParts = readInt(32);
            this.totalParts = readInt(36);

            this.livesLeft = readInt(40);
            this.lifePrice = readInt(44);

            this.readyToPlace = readBoolean(48);

            this.totalWave = readInt(52);
            this.currentWave = readInt(56);

            this.bonusReward.update(readLong(88));
        }

        @Override
        public boolean isOnMap() {
            return livesLeft > 0;
        }

        @Override
        public int getMultiplier() {
            // TODO: read multiplier
            return 0;
        }

        @Override
        public Optional<BonusReward> getBonusReward() {
            return bonusReward.valid ? Optional.of(bonusReward) : Optional.empty();
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private static class BonusRewardImpl extends Updatable implements GateInfo.BonusReward {
        private String lootId;
        private int amount, countdown;
        private boolean claimed, valid;

        private long lastUpdate;

        @Override
        public void update() {
            this.valid = address > 0;

            if (!valid) return;
            this.amount = readInt(32);
            this.claimed = readBoolean(36);
            this.countdown = readInt(40);
        }

        @Override
        public void update(long address) {
            boolean changed = address != this.address;
            super.update(address);

            if (changed && valid) {
                this.lootId = readString(48);
                this.lastUpdate = System.currentTimeMillis();
            }
        }

        public int getCountdown() {
            return (int) (countdown - (System.currentTimeMillis() - lastUpdate) / 1000);
        }
    }
}
