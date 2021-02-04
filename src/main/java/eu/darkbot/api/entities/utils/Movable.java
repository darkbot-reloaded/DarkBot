package eu.darkbot.api.entities.utils;

import eu.darkbot.api.entities.Entity;
import eu.darkbot.api.objects.Locatable;
import eu.darkbot.api.objects.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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
     * @return true if aims {code other} by checking their angle
     * @see #getAngle()
     */
    boolean isAiming(Locatable other);

    /**
     * @return {@link eu.darkbot.api.objects.Location} if has destination otherwise {@link Optional#empty()}.
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

    default long timeTo(@NotNull Locatable destination) {
        return timeTo(getLocationInfo().distanceTo(destination));
    }
}
