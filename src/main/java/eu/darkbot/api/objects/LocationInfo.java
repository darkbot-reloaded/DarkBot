package eu.darkbot.api.objects;

import eu.darkbot.api.entities.Entity;
import eu.darkbot.utils.Location;

public interface LocationInfo {
    boolean isLoaded();
    boolean isMoving();

    /**
     * @return speed of entity.
     */
    double getSpeed();

    double getAngle();
    double getAngle(Entity target);
    double getAngle(Location target);
    double getAngle(LocationInfo target);
    double getAngle(double x, double y);

    double distance(Entity target);
    double distance(Location target);
    double distance(LocationInfo target);
    double distance(double x, double y);

    Location getLocation();
    Location destinationInTime(long time);
}
