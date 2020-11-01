package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.BasePoint;
import eu.darkbot.api.objects.Gui;

/**
 * API for selling ores
 */
public interface OreTradeAPI extends Gui, API {

    /**
     * Sells the specified ore, trade window must be open for this method to work
     *
     * @param ore  the {@code Ore} you want to sell
     * @see #showTrade
     * @see #canSellOres
     */
    void sellOre(Ore ore);

    /**
     * Determines if ores can be sold based on if the ore trade window is open or not.
     *
     * @return true if ore trade window is open, false otherwise
     */
    boolean canSellOres();

    /**
     * Will either open or close the ore trade window based on the value of {@code show}
     *
     * @param show  true for showing ore trade window, false for closing ore trade window
     * @param base  the {@code BasePoint} of the ore trader base station
     * @return true if ore trader window has been opened or closed and its animation is done,
     *         false if animation is not done, or no action is needed to be taken
     *         to change the visibility status of the ore trader window
     */
    boolean showTrade(boolean show, BasePoint base);

    /**
     * List of Ores that you can sell through the Ore Trade window
     */
    enum Ore {
        PROMETIUM, ENDRIUM, TERBIUM, PROMETID, DURANIUM, PROMERIUM, SEPROM, PALLADIUM, OSMIUM
    }
}
