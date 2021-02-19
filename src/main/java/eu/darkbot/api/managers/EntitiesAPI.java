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
    Collection<? extends Npc> getNpcs();

    /**
     * @return {@link Collection} of {@link Pet}
     */
    Collection<? extends Pet> getPets();

    /**
     * @return {@link Collection} of {@link Ship}
     */
    Collection<? extends Ship> getPlayers();

    /**
     * @return {@link Collection} of {@link Box}
     */
    Collection<? extends Box> getBoxes();

    /**
     * @return {@link Collection} of {@link Mine}
     */
    Collection<? extends Mine> getMines();

    /**
     * @return {@link Collection} of {@link Portal}
     */
    Collection<? extends Portal> getPortals();

    /**
     * @return {@link Collection} of {@link Station}
     */
    Collection<? extends Station> getStations();

    /**
     * @return {@link Collection} of {@link BattleStation}
     */
    Collection<? extends BattleStation> getBattleStations();

    /**
     * @return {@link Collection} of {@link BattleStation.Module}
     */
    Collection<? extends BattleStation.Module> getBaseModules();

    /**
     * @return reference to {@link Collection} of all entities
     */
    Collection<? extends Entity> getAll();

    /**
     * @return {@link Collection} of unknown entities
     */
    Collection<? extends Entity> getUnknown();

    /**
     * @return {@link Collection} of {@link Obstacle}
     */
    Collection<? extends Obstacle> getObstacles();

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
