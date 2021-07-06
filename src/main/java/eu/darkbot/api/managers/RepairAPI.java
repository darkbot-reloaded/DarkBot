package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Collection;

public interface RepairAPI extends API.Singleton {

    /**
     * @return The amount of times you have been killed
     */
    int getDeathAmount();

    /**
     * @return if the hero is currently destroyed
     */
    boolean isDestroyed();

    /**
     * Tries to repair ship with given repair option
     *
     * @throws IllegalStateException if ship is already repaired
     */
    void tryRevive(int repairOption) throws IllegalStateException;

    /**
     * @return the list of available repairing options
     */
    Collection<Integer> getAvailableRepairOptions();

    /**
     * @return The name of the last thing that destroyed you.
     * This could be a player name or something else, eg: Mine, or an NPC name.
     */
    @Nullable String getLastDestroyerName();

    /**
     * @return Instant when the hero last died
     */
    @Nullable Instant getLastDeathTime();
}
