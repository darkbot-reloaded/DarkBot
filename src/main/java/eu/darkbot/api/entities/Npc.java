package eu.darkbot.api.entities;

public interface Npc extends Ship {

    /**
     * @return id of the npc
     */
    int getNpcId();

    /**
     * @return true if {@link Npc} has {@code InstaShield} effect
     */
    boolean hasInstaShield();
}
