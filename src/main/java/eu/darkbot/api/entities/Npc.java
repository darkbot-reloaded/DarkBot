package eu.darkbot.api.entities;

import eu.darkbot.api.config.NpcInfo;

public interface Npc extends Ship {

    /**
     * @return id of the npc
     */
    int getNpcId();

    /**
     * @return {@link NpcInfo} with some settings for {@link Npc}
     */
    NpcInfo getInfo();

}
