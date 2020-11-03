package eu.darkbot.api.entities;

import eu.darkbot.api.entities.utils.Map;
import org.jetbrains.annotations.Nullable;

/**
 * Represent in-game portal {@link Entity}
 */
public interface Portal extends Entity {

    /**
     * @return destination {@link Map} of this {@link Portal}
     */
    @Nullable
    Map getTargetMap();

    /**
     * Portal type id in generally is id of the portal's graphic.
     *
     * @return {@link Portal}'s type id
     */
    int getPortalType();

    // TODO: 01.11.2020 not sure which and when.
    Ship.Faction getFactionId();
}
