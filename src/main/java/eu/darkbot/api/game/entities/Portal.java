package eu.darkbot.api.game.entities;

import eu.darkbot.api.game.enums.PortalType;
import eu.darkbot.api.game.other.GameMap;
import eu.darkbot.api.game.other.EntityInfo;

import java.util.Optional;

/**
 * Represent in-game portal {@link Entity}
 */
public interface Portal extends Entity {

    /**
     * @return destination {@link GameMap} of this {@link Portal}
     */
    Optional<GameMap> getTargetMap();

    /**
     * @return portal type id
     */
    int getTypeId();

    /**
     * Portal type id in generally is id of the portal's graphic.
     *
     * @return {@link Portal}'s {@link PortalType}
     */
    default PortalType getPortalType() {
        return PortalType.of(getTypeId());
    }

    /**
     * Is {@link Portal} accessible only for a certain {@link EntityInfo.Faction}.
     * Mainly used for 5-x portals.
     *
     * @return the {@link EntityInfo.Faction} of the {@link Portal}
     */
    EntityInfo.Faction getFaction();

    /**
     * If the hero is currently jumping thru this portal
     * @return true if you're jumping, false otherwise
     */
    boolean isJumping();
}
