package eu.darkbot.api.entities;

import eu.darkbot.api.entities.other.Formation;
import eu.darkbot.api.entities.utils.Attacker;
import eu.darkbot.api.entities.utils.Movable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface Ship extends Attacker, Movable {

    /**
     * @return true if ship is invisible/cloaked.
     */
    boolean isInvisible();

    /**
     * @return true if this {@link Ship} is blacklisted
     */
    boolean isBlacklisted();

    /**
     * Adds this {@link Ship} to blacklist for given time(ms).
     *
     * @param forTime time in milliseconds
     */
    boolean setBlacklisted(long forTime);

    /**
     * @return true if ship has enabled {@link Pet}.
     */
    boolean hasPet();

    /**
     * @return {@link Pet} associated with this ship otherwise {@link Optional#empty()}.
     */
    Optional<Pet> getPet();

    /**
     * @return used {@link Formation} by the {@link Ship}
     */
    Formation getFormation();

    /**
     * @return true if ship has enabled given formation.
     */
    boolean isInFormation(int formationId);

    default boolean isInFormation(@NotNull Formation formation) {
        return isInFormation(formation.ordinal());
    }
}
