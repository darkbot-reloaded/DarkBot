package eu.darkbot.api.game.entities;

/**
 * In-game pet entity flying on the map
 */
public interface Pet extends Ship {

    /**
     * @return level of the {@link Pet}
     */
    int getLevel();

    /**
     * @return userId of the {@link Pet}'s owner
     */
    int getOwnerId();
}
