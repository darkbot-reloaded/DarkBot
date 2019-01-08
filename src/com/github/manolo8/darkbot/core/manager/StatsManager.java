package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;

import static com.github.manolo8.darkbot.Main.API;

public class StatsManager implements Manager {

    private long address;

    public double credits;
    public double uridium;
    public double experience;
    public double honor;

    private long started;
    private long runningTime;
    private boolean lastStatus;

    public double earnedCredits;
    public double earnedUridium;
    public double earnedExperience;
    public double earnedHonor;

    public String sid;

    private StringBuilder builder;

    public StatsManager(Main main) {
        builder = new StringBuilder();

        main.status.add(this::toggle);
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.userDataAddress.add(value -> {
            address = value;
            sid = API.readMemoryString(API.readMemoryLong(address + 168));
        });
    }

    @Override
    public void stop() {

    }

    public void tick() {
        if (address != 0) {
            updateCredits(API.readMemoryDouble(address + 288));
            updateUridium(API.readMemoryDouble(address + 296));
            updateExperience(API.readMemoryDouble(address + 312));
            updateHonor(API.readMemoryDouble(address + 320));
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

    public void reset() {
        earnedCredits = earnedExperience = earnedHonor = earnedUridium = 0;
    }

    public String runningTimeStr() {
        builder.setLength(0);

        int seconds = (int) (runningTime() / 1000);

        if (seconds > 3600) {

            int hours = seconds / 3600;

            if (hours < 10) {
                builder.append('0');
            }

            builder.append(hours).append(':');
        }

        if (seconds > 60) {

            int minutes = (seconds % 3600) / 60;

            if (minutes < 10) {
                builder.append('0');
            }

            builder.append(minutes).append(':');
        }

        seconds = seconds % 60;

        if (seconds < 10) {
            builder.append('0');
        }

        builder.append(seconds);

        return builder.toString();
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
