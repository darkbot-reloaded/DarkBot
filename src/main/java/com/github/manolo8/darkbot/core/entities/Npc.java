package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.NpcInfo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Npc extends Ship {
    private static final NpcInfo INITIAL_NPC_INFO = new NpcInfo(); // Prevent NPE trying to obtain npc info.

    public NpcInfo npcInfo = INITIAL_NPC_INFO;

    public Npc(int id) {
        super(id);
    }

    @Override
    public void update() {
        String oldName = playerInfo.username;

        super.update();

        //noinspection StringEquality
        if (oldName != playerInfo.username) {
            npcInfo = ConfigEntity.INSTANCE.getOrCreateNpcInfo(playerInfo.username);
        }
    }
}
