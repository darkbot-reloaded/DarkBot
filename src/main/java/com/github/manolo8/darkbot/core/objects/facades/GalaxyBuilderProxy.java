package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.BackpageManager;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.gui.GateSpinnerGui;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.utils.Time;
import eu.darkbot.api.game.galaxy.GalaxyGate;
import eu.darkbot.api.game.galaxy.GalaxyInfo;
import eu.darkbot.api.game.galaxy.GateInfo;
import eu.darkbot.api.game.galaxy.SpinResult;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.managers.BotAPI;
import eu.darkbot.api.managers.GalaxySpinnerAPI;
import eu.darkbot.util.Timer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GalaxyBuilderProxy extends Updatable implements GalaxySpinnerAPI {

    private final BotAPI bot;
    private final GateSpinnerGui gui;
    private final BackpageManager bpManager;

    @Getter
    private final BuilderData galaxyInfo = new BuilderData();

    private final Timer dirtyTimer = Timer.getRandom(250, 5);
    private final Timer guiUsed = Timer.getRandom(9_000, 1000);

    private int spinsUsed;
    private long lastSpinAttempt = 0;

    public GalaxyBuilderProxy(BotAPI bot, GateSpinnerGui gui, BackpageManager bpManager) {
        this.bot = bot;
        this.gui = gui;
        this.bpManager = bpManager;
    }

    @Override
    public void update() {
        this.galaxyInfo.update(readAtom(48));
        this.dirtyTimer.tryDisarm();

        // Last gui usage >10s ago, close gui
        if (bot.isRunning() && guiUsed.isInactive()) gui.show(false);
    }

    public boolean isWaiting() {
        return !galaxyInfo.initialized || dirtyTimer.isArmed();
    }

    @Override
    public @Nullable Boolean updateGalaxyInfos(int expiryTime) {
        return galaxyInfo.initialized;
    }

    @Override
    public Optional<SpinResult> spinGate(@NotNull GalaxyGate gate, boolean multiplier, int spinAmount, int minWait) {
        if (Thread.currentThread() == bpManager) {
            Time.sleep(lastSpinAttempt + minWait - System.currentTimeMillis());
            lastSpinAttempt = System.currentTimeMillis();
        }

        if (!setGate(gate) || !setSpinAmount(spinAmount) || (multiplier && !useMultiplier()) || !showGui()) {
            return Optional.empty();
        }

        Main.API.callMethodChecked(false, "23(26)008421700", 68, galaxyInfo.address); // make a spin
        spinsUsed += spinAmount;
        dirtyTimer.activate(5); // wait till next tick to spin again
        return Optional.of(new SpinResultImpl(gate));
    }

    @Override
    public int getSpinsUsed() {
        return spinsUsed;
    }

    @Override
    public boolean placeGate(@NotNull GalaxyGate gate, int minWait) {
        return prepareAndCall(gate, "23(26)008411600", 80, galaxyInfo.address);
    }

    @Override
    public boolean buyLife(@NotNull GalaxyGate gate, int minWait) {
        return prepareAndCall(gate, "23(26)008411600", 62, galaxyInfo.address);
    }

    private boolean setGate(GalaxyGate gate) {
        if (isWaiting()) return false;
        if (galaxyInfo.selectedGateId == gate.getId()) return true;
        prepareAndCall(null, "23(267)1016241700", 19, galaxyInfo.address, gate.getId());
        return false;
    }

    private boolean setSpinAmount(int amount) {
        if (isWaiting()) return false;
        if (galaxyInfo.selectedSpinAmount == amount) return true;
        if (prepareAndCall(null,"23(267)1016231600", 70, galaxyInfo.address, amount)) {
            dirtyTimer.activate();
        }
        return false;
    }

    private boolean showGui() {
        guiUsed.activate();
        return gui.show(true);
    }

    private boolean useMultiplier() {
        return prepareAndCall(null, "23(262)1016321600", 91, galaxyInfo.address, 1);
    }

    private boolean prepareAndCall(GalaxyGate gate, String signature, int index, long... arguments) {
        if (!isWaiting() && (gate == null || setGate(gate)) && showGui() &&
                Main.API.callMethodChecked(false, signature, index, arguments)) {
            dirtyTimer.activate();
            return true;
        }
        return false;
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
                int multiplier = readInt(224, 32);
                this.gatesArr.update(readLong(112));
                for (GalaxyGate gate : GalaxyGate.values()) {
                    GateInfoImpl gateInfo = gateData.get(gate);
                    gateInfo.update(gatesArr.getPtr(gate.getId() - 1));

                    if (isSelectedGate(gate)) {
                        // Current gate multiplier
                        gateInfo.setMultiplier(multiplier);
                    }
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

        private boolean isSelectedGate(GalaxyGate gate) {
            return selectedGateId > 0 && (selectedGateId == gate.getId() || gate.getId() <= 3 && selectedGateId <= 3);
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

        @Setter
        private int multiplier;

        @Override
        public void update() {
            if (address == 0) return;
            this.currentParts = readInt(32);
            this.totalParts = readInt(36);

            this.livesLeft = readInt(40);
            this.lifePrice = readInt(44);

            this.readyToPlace = readBoolean(48);

            this.totalWave = readInt(56);
            this.currentWave = readInt(60);

            this.bonusReward.update(readLong(88));
        }

        @Override
        public boolean isOnMap() {
            return livesLeft > 0;
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

    @Data
    private static class SpinResultImpl implements SpinResult {
        private final GalaxyGate galaxyGate;
        private Instant date = Instant.now();
        private int parts;
        private int multipliers;
        private SpinInfo mines = new SpinInfoImpl();
        private SpinInfo rockets = new SpinInfoImpl();
        private SpinInfo xenomit = new SpinInfoImpl();
        private SpinInfo nanoHull = new SpinInfoImpl();
        private SpinInfo logFiles = new SpinInfoImpl();
        private SpinInfo vouchers = new SpinInfoImpl();
        Map<SelectableItem.Laser, SpinInfo> ammo = new HashMap<>();
    }

    @Data
    private static class SpinInfoImpl implements SpinResult.SpinInfo {
        int obtained;
        int spinsUsed;
    }

}
