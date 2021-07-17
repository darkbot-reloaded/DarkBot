package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.game.entities.*;
import eu.darkbot.api.events.Event;
import eu.darkbot.api.game.other.Obstacle;

import java.util.Collection;

/**
 * API providing lists of the different types of entities in the map.
 *
 * This is arguably the most useful useful API in the whole bot, as it provides
 * the means to get information about all entities (ships, npcs, etc) the map.
 *
 * The instances provided in all of the methods are automatically updated by the
 * bot during runtime, and are unmodifiable collections.
 */
public interface EntitiesAPI extends API.Singleton {

    /**
     * @return All npcs visible in the map
     * @see Npc
     */
    Collection<? extends Npc> getNpcs();

    /**
     * @return All other player pets visible on the map
     * @see Pet
     */
    Collection<? extends Pet> getPets();

    /**
     * @return All other players visible on the map
     * @see Ship
     */
    Collection<? extends Ship> getPlayers();

    /**
     * @return All other players or pets visible on the map.
     * You can use {@link #getPlayers()} and {@link #getPets()} to get them separately
     * @see Ship
     * @see Pet
     */
    Collection<? extends Ship> getShips();

    /**
     * @return All boxes or ore resources visible on the map
     * @see Box
     * @see Ore
     */
    Collection<? extends Box> getBoxes();

    /**
     * @return All mines visible on the map
     * @see Mine
     */
    Collection<? extends Mine> getMines();

    /**
     * @return All portals available on the map.
     * @see Portal
     */
    Collection<? extends Portal> getPortals();

    /**
     * @return All home base stations, quest giver, and other demilitarized zones
     * @see Station for all the different types of bases
     */
    Collection<? extends Station> getStations();

    /**
     * @return All clan base stations parts in the map, including the modules
     * @see BattleStation
     * @see BattleStation.Module
     */
    Collection<? extends BattleStation> getBattleStations();

    /**
     * @return All entities that are detected by the bot, but not yet categorized into any other collection.
     *         Keep in mind whatever is in unknown now may always be properly categorized at a later date.
     */
    Collection<? extends Entity> getUnknown();

    /**
     * A collection view of a few other entities, that implement the {@link Obstacle} interface,
     * like clan base stations or mines.
     *
     * Keep in mind other (non-entity) obstacles may still exist outside of this, eg: player defined avoid areas.
     *
     * @return All entities implementing the {@link Obstacle} interface.
     * @see Obstacle
     */
    Collection<? extends Obstacle> getObstacles();

    /**
     * Keep in mind this is a special case in this API, as this list is NOT
     * guaranteed to be updated on runtime like all others.
     *
     * For performance reasons it is not advised to use this method unless necessary.
     *
     * @return A new collection with ALL of the entities provided by all the methods in this API, including unknown.
     */
    Collection<? extends Entity> getAll();

    /**
     * Base entity event triggered whenever any entity is added or removed.
     * To listen only for creation or removal, use the more specific events:
     * @see EntityCreateEvent
     * @see EntityRemoveEvent
     */
    abstract class EntityEvent implements Event {
        private final Entity entity;

        public EntityEvent(Entity entity) {
            this.entity = entity;
        }

        public Entity getEntity() {
            return entity;
        }
    }

    /**
     * Event fired when an entity is created (when it appears on the map)
     */
    class EntityCreateEvent extends EntityEvent {
        public EntityCreateEvent(Entity entity) {
            super(entity);
        }
    }

    /**
     * Event fired when an entity is removed (when it disappears from the map)
     */
    class EntityRemoveEvent extends EntityEvent  {
        public EntityRemoveEvent(Entity entity) {
            super(entity);
        }
    }

}
