package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.core.manager.EffectManager;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import eu.darkbot.api.game.entities.FakeEntity;
import eu.darkbot.api.game.other.Location;
import eu.darkbot.util.Timer;

import java.util.Objects;

import static com.github.manolo8.darkbot.Main.API;

public class Npc extends Ship implements eu.darkbot.api.game.entities.Npc {
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
        ish = hasEffect(EffectManager.Effect.NPC_ISH);

        if (!Objects.equals(oldName, playerInfo.username)) {
            npcInfo = ConfigEntity.INSTANCE.getOrCreateNpcInfo(playerInfo.username);
            npcInfo.npcId = npcId;
        }
    }

    @Override
    public int getNpcId() {
        return npcId;
    }

    @Override
    public eu.darkbot.api.config.types.NpcInfo getInfo() {
        return npcInfo;
    }

    // shipId for npcs is always 0
    @Override
    public int getShipId() {
        return getNpcId();
    }

    public static class FakeNpc extends Npc implements FakeEntity.FakeShip {
        private static int CURR_ID = Integer.MIN_VALUE;
        private Timer timeout;
        private long removeDistance;
        private boolean isRemoveWhenAttemptSelect;

        public FakeNpc(String npcName, Location loc, long removeDistance, long keepAlive, boolean isRemoveWhenAttemptSelect) {
            super(CURR_ID++);
            this.npcInfo = ConfigEntity.INSTANCE.getOrCreateNpcInfo(npcName);
            setLocation(loc);
            setRemoveDistance(removeDistance);
            setTimeout(keepAlive);
            setRemoveWhenAttemptSelect(isRemoveWhenAttemptSelect);
        }

        @Override
        public void setRemoveWhenAttemptSelect(boolean removeWhenAttemptSelect) {
            isRemoveWhenAttemptSelect = removeWhenAttemptSelect;
        }

        @Override
        public void setLocation(Location loc) {
            locationInfo.updatePosition(loc.x(), loc.y());
        }

        @Override
        public void setTimeout(long keepAlive) {
            if (keepAlive != -1) {
                timeout = Timer.get(keepAlive);
                timeout.activate();
            }
            else timeout = null;
        }

        @Override
        public void setRemoveDistance(long distance) {
            removeDistance = distance;
        }


        public boolean trySelect(boolean tryAttack) {
            if (isRemoveWhenAttemptSelect) removed();
            return false;
        }

        public boolean isInvalid(long mapAddress) {
            if (timeout != null && timeout.isInactive()) return false;
            return HeroManager.instance.distanceTo(this) < removeDistance;
        }

        public void update() {}

        public void update(long address) {}
    }
}
