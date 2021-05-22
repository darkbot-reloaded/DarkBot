package eu.darkbot.api.managers;

import eu.darkbot.api.API;

/**
 * API providing data for the mimesis mutinity event, escort gate in-game
 */
public interface EscortAPI extends API.Singleton {
    /**
     * The time displayed in-game, it can mean different things, like:
     *  - Time until the gate is open (lets players join), could be hours if the gate is closed until the next day
     *  - Time until the gate starts (waiting for players to join) usually 5 minutes
     *
     * @return Time left until next change, in seconds.
     */
    double getTime();

    /**
     * Entering the gate requires the use of one key.
     *
     * @return The current amount of keys.
     */
    double getKeys();
}
