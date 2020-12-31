package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.*;
import eu.darkbot.api.objects.Obstacle;
import eu.darkbot.api.utils.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * References to those collections doesn't change for whole runtime of the bot.
 */
public interface EntitiesAPI extends API {

    /**
     * @return {@link Collection} of {@link Npc}
     */
    Collection<Npc> getNpcs();

    /**
     * @return {@link Collection} of {@link Pet}
     */
    Collection<Pet> getPets();

    /**
     * @return {@link Collection} of {@link Ship}
     */
    Collection<Ship> getPlayers();

    /**
     * @return {@link Collection} of {@link Box}
     */
    Collection<Box> getBoxes();

    /**
     * @return {@link Collection} of {@link Mine}
     */
    Collection<Mine> getMines();

    /**
     * @return {@link Collection} of {@link Portal}
     */
    Collection<Portal> getPortals();

    /**
     * @return {@link Collection} of {@link Station}
     */
    Collection<? extends Station> getStations();

    /**
     * @return {@link Collection} of {@link BattleStation}
     */
    Collection<BattleStation> getBattleStations();

    /**
     * @return {@link Collection} of {@link BattleStation.Module}
     */
    Collection<BattleStation.Module> getBaseModules();

    /**
     * @return reference to {@link Collection} of all entities
     */
    Collection<Entity> getAll();

    /**
     * @return {@link Collection} of unknown entities
     */
    Collection<Entity> getUnknown();

    /**
     * @return {@link Collection} of {@link Obstacle}
     */
    Collection<Obstacle> getObstacles();

    /**
     * Adds listeners on create or remove of the {@link Entity}
     * <p>
     * Remember to store references for given listeners!
     * Otherwise they will be garbage collected.
     *
     * @param onCreate {@link Listener} which will be consumed on {@link Entity} creation.
     * @param onRemove {@link Listener} which will be consumed on {@link Entity} removal.
     * @see Listener
     */
    void addListener(@Nullable Listener<Entity> onCreate,
                     @Nullable Listener<Entity> onRemove);
}
