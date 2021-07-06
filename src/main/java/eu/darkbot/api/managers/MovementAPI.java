package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.Portal;
import eu.darkbot.api.objects.Locatable;
import eu.darkbot.api.objects.Location;
import org.jetbrains.annotations.NotNull;

public interface MovementAPI extends API.Singleton {

    /**
     * @return current hero location.
     * @see HeroAPI#getLocationInfo()
     */
    Location getCurrentLocation();

    /**
     * @return current destination {@link Location}.
     * If no path is being taken the current hero location will be the destination.
     */
    Location getDestination();

    /**
     * @return true if the hero is moving or trying to move in a path, false otherwise.
     */
    boolean isMoving();

    /**
     * @return true if hero is out of map
     */
    boolean isOutOfMap();

    /**
     * Checks if it is possible to move to your destination.
     * e.g. is inside the map, not inside a barrier or avoided zone.
     *
     * @return true if it is possible to move there.
     */
    boolean canMove(double x, double y);

    default boolean canMove(@NotNull Locatable destination) {
        return canMove(destination.getX(), destination.getY());
    }

    /**
     * Set the location to move towards.
     * This function will initiate pathfinding to search a route, then follow the route.
     * The route can be cancelled & cleared with {@link #stop(boolean)}.
     */
    void moveTo(double x, double y);

    default void moveTo(@NotNull Locatable destination) {
        moveTo(destination.getX(), destination.getY());
    }

    /**
     * Set the destination to a random position on the map.
     * If the user has selected `preferred zones`, a random point inside one of them will be used.
     */
    void moveRandom();

    /**
     * Stops the ship movement, and cleans-up the destination and path to take.
     *
     * @param currentLocation true to forcibly stop the ship in the current location
     *                        false to soft-stop, no more moves will be performed but
     *                          the ship may still continue to move towards the previous destination.
     */
    void stop(boolean currentLocation);

    /**
     * Tries jump through given {@link Portal}.
     *
     * You must be close enough to the portal to jump.
     * It is recommended to use a {@link eu.darkbot.shared.modules.utils.PortalJumper} instead,
     * it will cover more edge-cases.
     *
     * @param portal to jump through
     */
    void jumpPortal(Portal portal);

    /**
     * Calculate the distance to the closest valid point to a location
     * If {@link #canMove} is true, the distance will be 0, as the closest valid point is itself.
     * However if false, the closest valid location will be searched (moving out of barriers & inside map),
     * and the distance to that location will be returned.
     *
     * Example use cases:
     *  - Knowing the leniency for jumping a portal if the center is inside some obstacle
     *  - Ignoring non-moving NPCs if the closest distance is further away than lasers reach
     *
     * @return distance to the closest valid location.
     */
    double getClosestDistance(double x, double y);

    default double getClosestDistance(@NotNull Locatable destination) {
        return getClosestDistance(destination.getX(), destination.getY());
    }

    /**
     * Calculates distance between two points on the map,
     * including path-finding around obstacles & avoided zones.
     */
    double getDistanceBetween(double x, double y, double ox, double oy);

    default double getDistanceBetween(@NotNull Locatable loc, @NotNull Locatable otherLoc) {
        return getDistanceBetween(loc.getX(), loc.getY(), otherLoc.getX(), otherLoc.getY());
    }
}
