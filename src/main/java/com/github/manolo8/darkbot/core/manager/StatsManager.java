package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.itf.NativeUpdatable;
import com.github.manolo8.darkbot.modules.DisconnectModule;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.Time;
import eu.darkbot.api.managers.EventBrokerAPI;
import eu.darkbot.api.managers.StatsAPI;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.function.Consumer;

import static com.github.manolo8.darkbot.Main.API;

public class StatsManager implements Manager, StatsAPI, NativeUpdatable {

    private final Main main;
    private final EventBrokerAPI eventBroker;

    private final AverageStats cpuStat, pingStat, tickStat, memoryStat;

    @Deprecated
    public long currentBox; // Pretty out of place, but will work
    public double credits;
    public double uridium;
    public double experience;
    public double honor;
    public int novaEnergy;
    public int deposit;
    public int depositTotal;
    public int userId;
    public double earnedCredits;
    public double earnedUridium;
    public double earnedExperience;
    public double earnedHonor;
    public volatile String sid;
    public volatile String instance;

    private long address;
    private long started = System.currentTimeMillis();
    private long runningTime = Time.SECOND; // Assume running for 1 second by default
    private boolean lastStatus;

    public StatsManager(Main main, EventBrokerAPI eventBroker) {
        this.main = main;
        this.eventBroker = eventBroker;

        this.main.status.add(this::toggle);

        this.cpuStat = new AverageStats(true);
        this.pingStat = new AverageStats(false);
        this.tickStat = new AverageStats(true);
        this.memoryStat = new AverageStats(false);
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.invalid.add(value -> userId = 0);
        botInstaller.heroInfoAddress.add(value -> address = value);
    }

    public void tick() {
        if (address == 0) return;

        updateCredits(readDouble(352));
        updateUridium(readDouble(360));
        updateExperience(readDouble(376));
        updateHonor(readDouble(384));

        deposit = readIntHolder(304);
        depositTotal = readIntHolder(312);

        //currentBox = API.readMemoryLong(address + 0xE8);

        sid = readString(200);
        userId = readInt(48);

        //is that even possible to happen?
        if (main.settingsManager.getAddress() == 0) return;
        instance = main.settingsManager.readString(664);

        // Both memory location gives same value
        // long novaData = API.readMemoryLong(address + 0xC0) & ByteUtils.ATOM_MASK;
        // novaEnergy = API.readInt(novaData + 0x98);
        long novaData = readAtom(0x100);
        novaEnergy = API.readInt(novaData + 0x28);
    }

    public int getLevel() {
        return Math.max(1, (int) (Math.log(experience / 10_000) / Math.log(2)) + 2);
    }

    public void toggle(boolean running) {
        lastStatus = running;

        if (running) {
            started = System.currentTimeMillis();
        } else {
            runningTime += System.currentTimeMillis() - started;
        }
    }

    public void tickAverageStats(long timeDelta) {
        int p = getPing();
        if (p > 0) pingStat.accept(timeDelta, p);

        cpuStat.accept(timeDelta, API.getCpuUsage());
        tickStat.accept(timeDelta, main.getTickTime());
        memoryStat.accept(timeDelta, API.getMemoryUsage());
    }

    private void updateCredits(double credits) {
        double diff = credits - this.credits;

        if (this.credits != 0 && diff > 0 && updateStats()) {
            earnedCredits += diff;
        }

        this.credits = credits;
    }

    private void updateUridium(double uridium) {
        double diff = uridium - this.uridium;

        if (this.uridium != 0 && diff > 0 && updateStats()) {
            earnedUridium += diff;
        }

        this.uridium = uridium;
    }

    private void updateExperience(double experience) {
        if (experience == 0) return;
        if (this.experience != 0 && updateStats()) {
            earnedExperience += experience - this.experience;
        }
        this.experience = experience;
    }

    private void updateHonor(double honor) {
        if (honor == 0) return;
        double honorDiff = honor - this.honor;
        if (this.honor != 0 && updateStats()) {
            earnedHonor += honorDiff;
        }
        this.honor = honor;

        if (honorDiff > -10_000) return;

        System.out.println("Paused bot, lost " + honorDiff + " honor.");
        double friendlies = Math.log(Math.abs(honorDiff) / 100) / Math.log(2);
        boolean isExact = Math.abs(friendlies - Math.round(friendlies)) < 0.01;
        System.out.println("Look like " + friendlies + " friendly kills, credible & pausing: " + isExact);

        if (!main.config.MISCELLANEOUS.HONOR_LOST_EXACT || isExact)
            main.setModule(new DisconnectModule(null, I18n.get("module.disconnect.reason.honor")));
    }

    private boolean updateStats() {
        return main.isRunning() || main.config.MISCELLANEOUS.UPDATE_STATS_WHILE_PAUSED;
    }

    public long runningTime() {
        return runningTime + (lastStatus ? (System.currentTimeMillis() - started) : 0);
    }

    public double runningHours() {
        // Intentionally lose millisecond precision, in hopes of better double precision.
        long runningSeconds = runningTime / 1000;
        return runningSeconds / 3600d;
    }

    public double earnedCredits() {
        return earnedCredits / runningHours();
    }

    public double earnedUridium() {
        return earnedUridium / runningHours();
    }

    public double earnedExperience() {
        return earnedExperience / runningHours();
    }

    public double earnedHonor() {
        return earnedHonor / runningHours();
    }

    public void resetValues() {
        this.started = System.currentTimeMillis();
        this.runningTime = Time.SECOND;
        this.earnedCredits = 0;
        this.earnedUridium = 0;
        this.earnedHonor = 0;
        this.earnedExperience = 0;

        eventBroker.sendEvent(new StatsResetEvent());
    }

    @Override
    public int getPing() {
        return main.pingManager.ping;
    }

    @Override
    public Duration getRunningTime() {
        return Duration.ofMillis(runningTime());
    }

    @Override
    public int getCargo() {
        return deposit;
    }

    @Override
    public int getMaxCargo() {
        return depositTotal;
    }

    @Override
    public void resetStats() {
        resetValues();
    }

    @Override
    public double getTotalCredits() {
        return credits;
    }

    @Override
    public double getEarnedCredits() {
        return earnedCredits;
    }

    @Override
    public double getTotalUridium() {
        return uridium;
    }

    @Override
    public double getEarnedUridium() {
        return earnedUridium;
    }

    @Override
    public double getTotalExperience() {
        return experience;
    }

    @Override
    public double getEarnedExperience() {
        return earnedExperience;
    }

    @Override
    public double getTotalHonor() {
        return honor;
    }

    @Override
    public double getEarnedHonor() {
        return earnedHonor;
    }

    @Override
    public int getNovaEnergy() {
        return novaEnergy;
    }

    public AverageStats getCpuStats() {
        return cpuStat;
    }

    public AverageStats getPingStats() {
        return pingStat;
    }

    public AverageStats getTickStats() {
        return tickStat;
    }

    public AverageStats getMemoryStats() {
        return memoryStat;
    }

    @Override
    public long getAddress() {
        return address;
    }

    public static class AverageStats {
        private static final DecimalFormat ONE_PLACE_FORMAT = new DecimalFormat("0.0");

        private double last, average, max = Double.MIN_VALUE;
        private final boolean showDecimal;

        private Consumer<String> onChange;

        public AverageStats(boolean showDecimal) {
            this.showDecimal = showDecimal;
        }

        public void accept(long timeDelta, double value) {
            double adjustFactor = timeDelta / 10_000d;
            average = average + adjustFactor * (value - average);

            max += adjustFactor * 0.2 * (average - max);
            max = Math.max(max, value);

            if (last != value && onChange != null) {
                String s = showDecimal ? ONE_PLACE_FORMAT.format(value) : String.valueOf((int) value);
                onChange.accept(s);
            }
            last = value;
        }

        public double getMax() {
            return max;
        }

        public double getAverage() {
            return average;
        }

        public void setListener(Consumer<String> onChange) {
            this.onChange = onChange;
        }

        @Override
        public String toString() {
            return "Max=" + ONE_PLACE_FORMAT.format(getMax()) +
                    "\nAverage=" + ONE_PLACE_FORMAT.format(getAverage());
        }
    }
}
