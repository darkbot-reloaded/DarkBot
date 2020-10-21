package eu.darkbot.api.objects;

import eu.darkbot.utils.Location;

public interface Locatable {

    double getX();
    double getY();

    //maybe not necessary
    Location getLocation();
}
