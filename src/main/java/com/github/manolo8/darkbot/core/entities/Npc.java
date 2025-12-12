package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.core.entities.fake.FakeEntities;
import com.github.manolo8.darkbot.core.entities.fake.FakeExtension;
import com.github.manolo8.darkbot.core.manager.EffectManager;
import eu.darkbot.api.game.entities.FakeEntity;
import lombok.Getter;

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

        npcId = API.readInt(API.readLong(address + 192) + 80);
        ish = hasEffect(EffectManager.Effect.NPC_ISH);

        if (!Objects.equals(oldName, playerInfo.username)) {
            npcInfo = ConfigEntity.INSTANCE.getOrCreateNpcInfo(this);
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

    @Getter
    public static class Fake extends Npc implements FakeEntity.FakeNpc, FakeExtension {
        private final FakeExtension.Data fakeData = new FakeExtension.Data(this);
        public Fake(NpcInfo npcInfo) {
            super(FakeEntities.allocateFakeId());
            this.npcInfo = npcInfo;
            this.npcId = npcInfo.npcId;
            this.playerInfo.username = npcInfo.name;
        }

        @Override
        public boolean isInvalid(long mapAddress) {
            return fakeData.isInvalid();
        }

        @Override
        public boolean trySelect(boolean tryAttack) {
            return fakeData.trySelect(tryAttack);
        }

        @Override
        public void update() {
        }

        @Override
        public void update(long address) {
        }

    }
}
