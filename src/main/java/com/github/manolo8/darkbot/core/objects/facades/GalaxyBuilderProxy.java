package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.utils.Time;
import eu.darkbot.api.API;
import eu.darkbot.api.game.galaxy.GalaxyGate;

import java.util.ArrayList;
import java.util.List;

public class GalaxyBuilderProxy extends Updatable implements API.Singleton {

    private final BuilderData builderData = new BuilderData();

    @Override
    public void update() {
        this.builderData.update(readAtom(48));
    }

    // 1, 5, 10, 100
    public void spin(GalaxyGate gate, int amount) {
        if (!builderData.initialized) return;

        Main.API.callMethodAsync(19, builderData.address, gate.getId()); // set current gate
        Main.API.callMethodAsync(70, builderData.address, amount); // set spin amount
        Main.API.callMethodAsync(68, builderData.address); // make a spin
    }

    @Override
    public String toString() {
        return "GalaxyBuilderProxy{" +
                "builderData=" + builderData +
                '}';
    }

    private static class BuilderData extends Updatable.Auto {
        private final ObjArray gatesArr = ObjArray.ofArrObj(true);
        private final List<GateData> gateData = new ArrayList<>();
        private int freeSpins, selectedSpinAmount, spinPrice, selectedGateId;
        private boolean initialized;

        private double uridium;

        @Override
        public void update() {
            this.freeSpins = readInt(64);
            this.spinPrice = readInt(68);
            this.selectedGateId = readInt(76);

            this.initialized = readBoolean(80);
            this.selectedSpinAmount = readInt(96);

            this.gatesArr.update(readLong(112));
            this.gatesArr.sync(gateData, GateData::new);

            this.uridium = readDouble(288);
        }

        @Override
        public String toString() {
            return "BuilderData{" +
                    "gatesArr=" + gatesArr +
                    ", gateData=" + gateData +
                    ", freeSpins=" + freeSpins +
                    ", selectedSpinAmount=" + selectedSpinAmount +
                    ", spinPrice=" + spinPrice +
                    ", selectedGateId=" + selectedGateId +
                    ", initialized=" + initialized +
                    ", uridium=" + uridium +
                    '}';
        }
    }

    private static class GateData extends Updatable.Auto {
        private final BonusReward bonusReward = new BonusReward();
        private int currentParts, totalParts;
        private int livesLeft, livePrice;
        private int currentWave, totalWave;
        private boolean readyToPlace;

        @Override
        public void update() {
            if (address == 0) return;
            this.currentParts = readInt(32);
            this.totalParts = readInt(36);

            this.livesLeft = readInt(40);
            this.livePrice = readInt(44);

            this.readyToPlace = readBoolean(48);

            this.totalWave = readInt(52);
            this.currentWave = readInt(56);

            this.bonusReward.update(readLong(88));
        }

        @Override
        public String toString() {
            return "GateData{" +
                    "bonusReward=" + bonusReward +
                    ", currentParts=" + currentParts +
                    ", totalParts=" + totalParts +
                    ", livesLeft=" + livesLeft +
                    ", livePrice=" + livePrice +
                    ", currentWave=" + currentWave +
                    ", totalWave=" + totalWave +
                    ", readyToPlace=" + readyToPlace +
                    '}';
        }
    }

    private static class BonusReward extends Updatable.Auto {
        private String lootId;
        private int amount, countdown, countdownTimer;

        private boolean claimed, valid;

        private long localTime;

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

            if (changed) {
                this.lootId = readString(48);
                this.localTime = System.currentTimeMillis();
                //this.countdownTimer = readInt(44);
            }
        }

        public String getCountdown() {
            return Time.secondsToString((int) (countdown - (System.currentTimeMillis() - localTime) / 1000));
        }

        @Override
        public String toString() {
            return "BonusReward{" +
                    "lootId='" + lootId + '\'' +
                    ", amount=" + amount +
                    ", countdown=" + countdown +
                    ", countdownTimer=" + countdownTimer +
                    ", claimed=" + claimed +
                    ", valid=" + valid +
                    ", localTime=" + localTime +
                    '}';
        }
    }
}
