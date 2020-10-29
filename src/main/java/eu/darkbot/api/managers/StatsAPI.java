package eu.darkbot.api.managers;

import eu.darkbot.api.API;

public interface StatsAPI extends API {

    int getLevel();
    long getRunningTime();

    int getCargo();
    int getMaxCargo();

    double getTotalCredits();
    double getEarnedCredits();

    double getTotalUridium();
    double getEarnedUridium();

    double getTotalExperience();
    double getEarnedExperience();

    double getTotalHonor();
    double getEarnedHonor();
}
