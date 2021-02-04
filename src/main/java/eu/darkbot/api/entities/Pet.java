package eu.darkbot.api.entities;

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
