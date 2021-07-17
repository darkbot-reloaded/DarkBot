package eu.darkbot.api.game.other;

import eu.darkbot.api.game.entities.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * In-game entity that is able to move on the map
 */
public interface Movable extends Entity {

    /**
     * @return speed of the {@link Movable} in-game.
     */
    int getSpeed();

    /**
     * @return angle of the {@link Movable} in-game as radians.
     */
    double getAngle();

    /**
     * @return true if this entity is visually aiming at another by checking the angle
     * @see #getAngle()
     */
    boolean isAiming(Locatable other);

    /**
     * @return The current traveling destination of the entity if any, otherwise {@link Optional#empty()}.
     */
    Optional<Location> getDestination();

    /**
     * Calculates needed time to travel given distance.
     *
     * @return time in milliseconds needed to travel given distance
     */
    default long timeTo(double distance) {
        return (long) (distance * 1000 / getSpeed());
    }

    /**
     * The time it will take for the entity to move to the desired destination
     * @param destination the position to move to
     * @return time in milliseconds needed to reach the destination
     */
    default long timeTo(@NotNull Locatable destination) {
        return timeTo(getLocationInfo().distanceTo(destination));
    }
}
