package eu.darkbot.api.game.group;

import eu.darkbot.api.game.other.Health;

/**
 * Group member info
 */
public interface MemberInfo extends Health {

    int getShipType();

    String getUsername();
}
