package eu.darkbot.api.entities;

import eu.darkbot.api.entities.utils.Attacker;
import eu.darkbot.api.entities.utils.Movable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface Ship extends Attacker, Movable {

    /**
     * @return true if ship is invisible/cloaked.
     */
    boolean isInvisible();

    /**
     * @return true if this {@link Ship} is blacklisted
     */
    boolean isBlacklisted();

    /**
     * Adds this {@link Ship} to blacklist for given time(ms).
     *
     * @param forTime time in milliseconds
     */
    boolean setBlacklisted(long forTime);

    /**
     * @return true if ship has enabled {@link Pet}.
     */
    boolean hasPet();

    /**
     * @return {@link Pet} associated with this ship otherwise {@link Optional#empty()}.
     */
    Optional<Pet> getPet();

    /**
     * @return used {@link Formation} by the {@link Ship}
     */
    Formation getFormation();

    /**
     * @return true if ship has enabled given formation.
     */
    boolean isInFormation(int formationId);

    default boolean isInFormation(@NotNull Ship.Formation formation) {
        return isInFormation(formation.ordinal());
    }

    /**
     * Represents formations in-game.
     */
    enum Formation {
        // 2D formations
        STANDARD,
        TURTLE(0.1),
        ARROW,
        LANCE,
        STAR,
        PINCER,
        DOUBLE_ARROW(-0.2),
        DIAMOND(-0.3, 0, 0.01),
        CHEVRON(-0.2, 0),
        MOTH(0.2, 0, -0.05),
        CRAB,
        HEART(0.2, 0.2),
        BARRAGE,
        BAT,
        // 3D formations
        RING(0.85),
        DRILL(-0.25),
        VETERAN(-0.2, -0.2),
        DOME(0, 0.3, 0.005),
        WHEEL(0, 0, -0.05),
        X(0.08, 0),
        WAVY,
        MOSQUITO;

        private double hp, sh, sps;

        Formation() {
        }

        Formation(double sh) {
            this.sh = sh;
        }

        Formation(double hp, double sh) {
            this.hp = hp;
            this.sh = sh;
        }

        Formation(double hp, double sh, double sps) {
            this.hp = hp;
            this.sh = sh;
            this.sps = sps;
        }

        public double getShieldMultiplier() {
            return sh;
        }

        public double getHealthMultiplier() {
            return hp;
        }

        /**
         * @return shield regen % amount per second.
         */
        public double getShieldRegen() {
            return sps;
        }
    }
}
