package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.objects.Gui;

/**
 * API for targeted offers or in-game ads
 */
public interface TargetedOfferAPI extends Gui, API {
    /**
     * Will attempt to close the targeted offer
     *
     * @param visible  only false value is accepted, to close the window
     * @return false if targeted offer window has been closed
     *         else it returns if the animation is done
     * @throws UnsupportedOperationException if {@code visible} is true
     */
    @Override
    boolean setVisible(boolean visible);
}
