package eu.darkbot.api.game.entities;

import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.game.other.Attacker;
import eu.darkbot.api.game.other.Movable;
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
     * If this ship has been blacklisted by calling {@link #setBlacklisted}
     *
     * @return if this {@link Ship} is currently blacklisted, false if blacklist has expired
     */
    boolean isBlacklisted();

    /**
     * Adds this {@link Ship} to blacklist for given time (ms).
     * The main use-case is remembering this enemy ship attacked you, but can be used
     * for other purposes like NPCs that are bugged or have been attacked by others.
     *
     * By itself, this changes nothing but the response of {@link #isBlacklisted}
     *
     * @param time time in milliseconds to stay in the blacklist
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
