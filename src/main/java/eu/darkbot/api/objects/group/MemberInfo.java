package eu.darkbot.api.objects.group;

import eu.darkbot.api.objects.Health;

/**
 * Group member info
 */
public interface MemberInfo extends Health {
    int getShipType();
    String getUsername();
}
