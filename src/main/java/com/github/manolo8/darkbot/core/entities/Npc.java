package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.NpcInfo;

import static com.github.manolo8.darkbot.Main.API;

public class Npc extends Ship {
    private static final NpcInfo INITIAL_NPC_INFO = new NpcInfo(); // Prevent NPE trying to obtain npc info.

    public NpcInfo npcInfo = INITIAL_NPC_INFO;
    public int npcId;
    public boolean ish;

    public Npc(int id) {
        super(id);
    }

    public Npc(int id, long address) {
        super(id);
        this.update(address);
    }

    @Override
    public void update() {
        String oldName = playerInfo.username;
        super.update();

        npcId = API.readMemoryInt(API.readMemoryLong(address + 192) + 80);
        //  The address is wrong, and likely belongs to a "has any effect" boolean, but the other ISH effect is wrong
        ish = API.readMemoryBoolean(API.readMemoryLong(address + 208) + 56);
        // This ISH effect, is not the one used in NPCs.
        // ish = hasEffect(EffectManager.Effect.ISH);

        if (!oldName.equals(playerInfo.username)) {
            npcInfo = ConfigEntity.INSTANCE.getOrCreateNpcInfo(playerInfo.username);
            npcInfo.npcId = npcId;
        }
    }
}
