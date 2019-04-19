package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;

import static com.github.manolo8.darkbot.Main.API;

public class StatsManager implements Manager {

    private long address;
    private long settingsAddress;

    public double credits;
    public double uridium;
    public double experience;
    public double honor;
    public int deposit;
    public int depositTotal;

    private long started = System.currentTimeMillis();
    private long runningTime = 1;
    private boolean lastStatus;

    public double earnedCredits;
    public double earnedUridium;
    public double earnedExperience;
    public double earnedHonor;

    public volatile String sid;
    public volatile String instance;

    public StatsManager(Main main) {
        main.status.add(this::toggle);
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.userDataAddress.add(value -> {
            address = value;
            sid = API.readMemoryString(API.readMemoryLong(address + 168));
        });
        botInstaller.settingsAddress.add(value -> {
            settingsAddress = value;
            instance = null;
        });
    }


    public void tick() {
        if (address == 0) return;
        updateCredits(API.readMemoryDouble(address + 288));
        updateUridium(API.readMemoryDouble(address + 296));
        //API.readMemoryDouble(address + 304); // Jackpot
        updateExperience(API.readMemoryDouble(address + 312));
        updateHonor(API.readMemoryDouble(address + 320));

        deposit = API.readMemoryInt(API.readMemoryLong(address + 240) + 40);
        depositTotal = API.readMemoryInt(API.readMemoryLong(address + 248) + 40);

        if (settingsAddress == 0) return;
        if (instance == null || instance.isEmpty() || !instance.startsWith("http")) {
            instance = API.readMemoryString(API.readMemoryLong(settingsAddress + 588));
        }
    }


    public void toggle(boolean running) {
        lastStatus = running;

        if (running) {
            started = System.currentTimeMillis();
        } else {
            runningTime += System.currentTimeMillis() - started;
        }
    }

    private void updateCredits(double credits) {
        double diff = credits - this.credits;

        if (this.credits != 0 && diff > 0) {
            earnedCredits += diff;
        }

        this.credits = credits;
    }

    private void updateUridium(double uridium) {
        double diff = uridium - this.uridium;

        if (this.uridium != 0 && diff > 0) {
            earnedUridium += diff;
        }

        this.uridium = uridium;
    }

    private void updateExperience(double experience) {
        if (this.experience != 0) earnedExperience += experience - this.experience;
        this.experience = experience;
    }

    private void updateHonor(double honor) {
        if (this.honor != 0) earnedHonor += honor - this.honor;
        this.honor = honor;
    }

    public long runningTime() {
        return runningTime + (lastStatus ? (System.currentTimeMillis() - started) : 0);
    }

    public double earnedCredits() {
        return earnedCredits / ((double) runningTime() / 3600000);
    }

    public double earnedUridium() {
        return earnedUridium / ((double) runningTime() / 3600000);
    }

    public double earnedExperience() {
        return earnedExperience / ((double) runningTime() / 3600000);
    }

    public double earnedHonor() {
        return earnedHonor / ((double) runningTime() / 3600000);
    }
}
