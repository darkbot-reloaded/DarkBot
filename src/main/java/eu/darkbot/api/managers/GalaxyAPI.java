package eu.darkbot.api.managers;

import eu.darkbot.api.objects.galaxy.GateInfo;
import eu.darkbot.api.objects.galaxy.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface GalaxyAPI {

    Boolean updateInfo(int expiryTime);

    GateInfo getGateInfo(Gate gate);

    @Nullable
    GateInfo placeGate(Gate gate, int minWait);

    @Nullable
    Collection<Item> spinGate(Gate gate, int useMultiAt, int spinAmount, int minWat);

    int getUridium();
    int getSamples();
    int getEnergyCost();
    int getSpinAmount();
    int getSpinSalePercentage();

    boolean isSpinSale();
    boolean isGalaxyGateDay();
    boolean isBonusRewardDay();

    /**
     * Represents in-game 'craftable' maps.
     */
    enum Gate {
        ALPHA  (1,  "alpha",    "α"),
        BETA   (2,  "beta",     "β"),
        GAMMA  (3,  "gamma",    "γ"),
        DELTA  (4,  "delta",    "δ"),
        EPSILON(5,  "epsilon",  "ε"),
        ZETA   (6,  "zeta",     "ζ"),
        KAPPA  (7,  "kappa",    "κ"),
        LAMBDA (8,  "lambda",   "λ"),
        KRONOS (12, "kronos",   "Kronos"),
        HADES  (13, "hades",    "Hades"),
        KUIPER (19, "streuner", "ς");

        private final int id;
        private final String name;
        private final String symbol;

        Gate(int id, String name, String symbol) {
            this.name = name;
            this.id   = id;
            this.symbol = symbol;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSymbol() {
            return symbol;
        }
    }
}
