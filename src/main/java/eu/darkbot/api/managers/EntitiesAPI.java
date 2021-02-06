package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.*;
import eu.darkbot.api.events.Event;
import eu.darkbot.api.objects.Obstacle;

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

    class EntityEvent implements Event {
        private final Entity entity;

        public EntityEvent(Entity entity) {
            this.entity = entity;
        }

        public Entity getEntity() {
            return entity;
        }
    }

    class EntityCreateEvent extends EntityEvent {
        public EntityCreateEvent(Entity entity) {
            super(entity);
        }
    }

    class EntityRemoveEvent extends EntityEvent  {
        public EntityRemoveEvent(Entity entity) {
            super(entity);
        }
    }

}
