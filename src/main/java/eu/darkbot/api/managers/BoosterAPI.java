package eu.darkbot.api.managers;

import eu.darkbot.api.objects.Booster;
import eu.darkbot.api.objects.Gui;

import java.util.List;

/**
 * API for boosters (seeing which boosters are currently active, how much time they have, etc.)
 */
public interface BoosterAPI extends Gui {
    /**
     * @return {@code List} of all Boosters currently active on ship
     */
    List<Booster> getBoosters();
}
