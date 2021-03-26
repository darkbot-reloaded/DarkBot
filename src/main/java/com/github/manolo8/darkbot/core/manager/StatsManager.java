package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.modules.DisconnectModule;
import com.github.manolo8.darkbot.utils.I18n;
import eu.darkbot.api.managers.StatsAPI;

import static com.github.manolo8.darkbot.Main.API;

public class StatsManager implements Manager, StatsAPI {

    private Main main;

    private long address;
    private long settingsAddress;

    public long currentBox; // Pretty out of place, but will work

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
        this.main = main;
        this.main.status.add(this::toggle);
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.heroInfoAddress.add(value -> address = value);
        botInstaller.settingsAddress.add(value -> settingsAddress = value);
    }


    public void tick() {
        if (address == 0) return;
        updateCredits(API.readMemoryDouble(address + 296));
        updateUridium(API.readMemoryDouble(address + 304));
        //API.readMemoryDouble(address + 312); // Jackpot
        updateExperience(API.readMemoryDouble(address + 320));
        updateHonor(API.readMemoryDouble(address + 328));

        deposit = API.readMemoryInt(API.readMemoryLong(address + 248) + 40);
        depositTotal = API.readMemoryInt(API.readMemoryLong(address + 256) + 40);

        currentBox = API.readMemoryLong(address + 0xE8);

        sid = API.readMemoryStringFallback(API.readMemoryLong(address + 176), null);
        if (settingsAddress == 0) return;
        instance = API.readMemoryStringFallback(API.readMemoryLong(settingsAddress + 616), null);
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
        if (experience == 0) return;
        if (this.experience != 0) earnedExperience += experience - this.experience;
        this.experience = experience;
    }

    private void updateHonor(double honor) {
        if (honor == 0) return;
        double honorDiff = honor - this.honor;
        if (this.honor != 0) earnedHonor += honorDiff;
        this.honor = honor;

        if (honorDiff > -10_000) return;

        System.out.println("Paused bot, lost " + honorDiff + " honor.");
        double friendlies = Math.log(Math.abs(honorDiff) / 100) / Math.log(2);
        boolean isExact = Math.abs(friendlies - Math.round(friendlies)) < 0.01;
        System.out.println("Look like " + friendlies + " friendly kills, credible & pausing: " + isExact);

        if (!main.config.MISCELLANEOUS.HONOR_LOST_EXACT || isExact)
            main.setModule(new DisconnectModule(null, I18n.get("module.disconnect.reason.honor")));
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

    public void resetValues() {
        started = System.currentTimeMillis();
        runningTime = 1;
        earnedCredits = 0;
        earnedUridium = 0;
        earnedHonor = 0;
        earnedExperience = 0;
    }

    @Override
    public int getPing() {
        return main.pingManager.ping;
    }

    @Override
    public long getRunningTime() {
        return runningTime();
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
}
