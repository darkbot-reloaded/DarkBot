package eu.darkbot.api.managers;

public interface StatsManager {

    int getLevel();
    long getRuntime();

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
