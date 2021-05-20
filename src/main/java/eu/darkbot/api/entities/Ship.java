package eu.darkbot.api.entities;

import eu.darkbot.api.entities.other.SelectableItem;
import eu.darkbot.api.entities.utils.Attacker;
import eu.darkbot.api.entities.utils.Movable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * In-game generic ship on the map, like players, npc, pets and more.
 */
public interface Ship extends Attacker, Movable {

    /**
     * @return true if ship is invisible/cloaked.
     */
    boolean isInvisible();

    /**
     * @return true if this {@link Ship} is blacklisted, which can mean a number of things
     */
    boolean isBlacklisted();

    /**
     * Adds this {@link Ship} to blacklist for given time (ms).
     * The main use-case is remembering this ship attacked you, but can be used for other purposes.
     *
     * @param time time in milliseconds
     */
    void setBlacklisted(long time);

    /**
     * @return if this ship has a {@link Pet} enabled flying on the map.
     */
    boolean hasPet();

    /**
     * @return {@link Pet} associated with this ship otherwise {@link Optional#empty()}.
     */
    Optional<Pet> getPet();

    /**
     * @return the {@link SelectableItem.Formation} currently in use by this {@link Ship}, or
     *          {@link SelectableItem.Formation#STANDARD} otherwise.
     */
    SelectableItem.Formation getFormation();

    /**
     * @return if the ship is flying the given formation by id.
     */
    boolean isInFormation(int formationId);

    /**
     * @return if the ship is flying the given formation.
     */
    default boolean isInFormation(@NotNull SelectableItem.Formation formation) {
        return isInFormation(formation.ordinal());
    }
}
