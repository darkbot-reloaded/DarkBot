package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.Station;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * API for ore related things such as selling ores or getting ore amount.
 */
public interface OreAPI extends API {

    /**
     * @param ore or to check
     * @return amount of owned {@link Ore}
     */
    int getAmount(@NotNull Ore ore);

    /**
     * Sells the specified ore, trade window must be open for this method to work
     *
     * @param ore the {@code Ore} you want to sell it must be {@code sellable}
     * @see #showTrade
     * @see #canSellOres
     * @see Ore#sellable
     */
    void sellOre(@NotNull Ore ore);

    /**
     * Determines if ores can be sold based on if the ore trade window is open or not.
     *
     * @return true if ore trade window is open, false otherwise
     */
    boolean canSellOres();

    /**
     * Will either open or close the ore trade window based on the value of {@code show}
     *
     * @param show       true for showing ore trade window, false for closing ore trade window
     * @param tradePoint the {@code BasePoint} of the ore trader base station
     * @return true if ore trader window has been opened or closed and its animation is done,
     * false if animation is not done, or no action is needed to be taken
     * to change the visibility status of the ore trader window
     */
    boolean showTrade(boolean show, @NotNull Station.Refinery tradePoint);

    /**
     * Types of Ores visible in refinery window
     */
    enum Ore {
        PROMETIUM,
        ENDRIUM,
        TERBIUM,
        PROMETID,
        DURANIUM,
        PROMERIUM,
        SEPROM,
        PALLADIUM,
        OSMIUM,
        XENOMIT(false);

        private final boolean sellable;

        Ore() {
            this(true);
        }

        Ore(boolean sellable) {
            this.sellable = sellable;
        }

        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public boolean isSellable() {
            return sellable;
        }
    }
}
