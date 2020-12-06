package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.BasePoint;
import eu.darkbot.api.entities.BattleStation;
import eu.darkbot.api.entities.Box;
import eu.darkbot.api.entities.Entity;
import eu.darkbot.api.entities.Mine;
import eu.darkbot.api.entities.Npc;
import eu.darkbot.api.entities.Pet;
import eu.darkbot.api.entities.Portal;
import eu.darkbot.api.entities.Ship;
import eu.darkbot.api.objects.Obstacle;
import eu.darkbot.api.utils.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public interface EntitiesAPI extends API {

    /**
     * @return {@link BattleStation} if present, otherwise {@link Optional#empty()}
     */
    Optional<BattleStation> getBattleStation();

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
     * @return {@link Collection} of {@link BasePoint}
     */
    Collection<BasePoint> getBasePoints();

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
     * Use {@code Entity.class} to get {@link Collection} of all entities.
     *
     * @param entityType to get collection of
     * @return {@link Collection} of given entity type
     */
    <T extends Entity>
    Collection<T> get(Class<T> entityType);

    /**
     * Adds listeners on create or remove of the {@link Entity}
     * <p>
     * Remember to store references for given listeners!
     * Otherwise they will be garbage collected.
     *
     * @param onCreate {@link Listener} which can be null
     * @param onRemove {@link Listener} which can be null
     * @see Listener
     */
    void addListener(@Nullable Listener<Entity> onCreate,
                     @Nullable Listener<Entity> onRemove);

}
