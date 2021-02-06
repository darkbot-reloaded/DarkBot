package eu.darkbot.api.entities;

import eu.darkbot.api.entities.other.PortalType;
import eu.darkbot.api.entities.utils.Map;
import eu.darkbot.api.objects.EntityInfo;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Represent in-game portal {@link Entity}
 */
public interface Portal extends Entity {

    /**
     * @return destination {@link Map} of this {@link Portal}
     */
    Optional<Map> getTargetMap();

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
}
