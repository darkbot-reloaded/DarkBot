package eu.darkbot.api.game.entities;

import eu.darkbot.api.config.NpcInfo;

/**
 * In-game non-player-controlled entity, aka aliens
 */
public interface Npc extends Ship {

    /**
     * @return id of the npc
     */
    int getNpcId();

    /**
     * @return {@link NpcInfo} with some user settings for {@link Npc}
     */
    NpcInfo getInfo();

}
