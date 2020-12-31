package eu.darkbot.api.entities;

import eu.darkbot.api.entities.other.PortalType;
import eu.darkbot.api.entities.utils.Map;
import eu.darkbot.api.objects.Info;
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
     * Portal type id in generally is id of the portal's graphic.
     *
     * @return {@link Portal}'s {@link PortalType}
     */
    @Nullable
    PortalType getPortalType();

    // TODO: 01.11.2020 not sure which and when.
    Info.Fraction getFactionId();

}
