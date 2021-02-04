package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Collection;

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

    @Nullable String getLastDestroyerName();

    @Nullable Instant getLastDeathTime();
}
