package eu.darkbot.api.managers;

import eu.darkbot.api.API;

/**
 * API to get info about hero statistics, running time, current/earned money etc.
 */
public interface StatsAPI extends API {

    /**
     * @return current hero level
     */
    int getLevel();

    /**
     * @return running time in milliseconds.
     */
    long getRunningTime();

    /**
     * @return current amount of cargo
     */
    int getCargo();

    /**
     * @return max amount of cargo
     */
    int getMaxCargo();

    /**
     * @return total amount of credits which hero currently have
     */
    double getTotalCredits();

    /**
     * @return earned amount of credits while bot was working
     */
    double getEarnedCredits();

    /**
     * @return total amount of uridium which hero currently have
     */
    double getTotalUridium();

    /**
     * @return earned amount of uridium while bot was working
     */
    double getEarnedUridium();

    /**
     * @return total amount of experience which hero currently have
     */
    double getTotalExperience();

    /**
     * @return earned amount of experience while bot was working
     */
    double getEarnedExperience();

    /**
     * @return total amount of honor which hero currently have
     */
    double getTotalHonor();

    /**
     * @return earned amount of honor while bot was working
     */
    double getEarnedHonor();
}
