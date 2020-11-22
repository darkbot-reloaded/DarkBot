package eu.darkbot.api.managers;

import eu.darkbot.api.API;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface RepairAPI extends API {

    int getDeathsAmount();

    boolean isDestroyed();

    /**
     * Tries to repair ship with given repair option
     *
     * @throws IllegalStateException if ship is already repaired
     */
    void tryRevive(int repairOption) throws IllegalStateException;

    Collection<Integer> getAvailableRepairOptions();

    Optional<String> getLastDestroyerName();

    Optional<LocalDateTime> getLastDeathTime();
}
