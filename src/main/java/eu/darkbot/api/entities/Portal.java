package eu.darkbot.api.entities;

import eu.darkbot.api.entities.utils.Map;

public interface Portal extends Entity {
    Map getTargetMap();

    int getPortalType();
    int getFactionId();
}
