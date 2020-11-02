package eu.darkbot.api.managers;

import eu.darkbot.api.API;

/**
 * API for fps and memory usage
 */
public interface PerformanceAPI extends API {
    int getFps();
    int getMemory();
}
