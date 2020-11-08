package eu.darkbot.api.managers;

import eu.darkbot.api.API;

/**
 * API for fps and memory usage
 */
public interface PerformanceAPI extends API {

    /**
     * @return in-game FPS.
     */
    int getFps();

    /**
     * @return memory used by current process in MB
     */
    int getMemory();

    /**
     * Gets current ping.
     * Ping is updated every 15 seconds.
     *
     * @return current ping in milliseconds.
     */
    int getPing();
}
