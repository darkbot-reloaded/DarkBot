package eu.darkbot.api.game.entities;

import eu.darkbot.api.game.other.Attackable;
import eu.darkbot.api.game.other.Obstacle;

/**
 * In-game clan base station or meteoroid
 */
public interface BattleStation extends Obstacle, Attackable {

    /**
     * In-game id for the visual hull type.
     * Id 0 is an empty meteoroid, values 1 to 255 are built bases, other values are invalid.
     *
     * @return the in-game hull id for this meteoroid
     */
    int getHullId();

    /**
     * In-game battle station modules around the main battle station
     */
    interface Module extends Obstacle, Attackable /*should be attacker but...*/ {

        /**
         * @return the in-game id of the module
         */
        String getModuleId();
    }
}
