package com.github.manolo8.darkbot.core.objects.group;

import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.manager.HeroManager;

import static com.github.manolo8.darkbot.Main.API;

public class Invite extends UpdatableAuto {
    private final HeroManager hero;

    public PartialGroupMember inviter = new PartialGroupMember();
    public PartialGroupMember invited = new PartialGroupMember();
    public boolean incomming;

    public Invite(HeroManager hero) {
        this.hero = hero;
    }

    @Override
    public void update() {
        if (address == 0) return;
        inviter.update(API.readMemoryLong(address + 0x20));
        invited.update(API.readMemoryLong(address + 0x28));
        incomming = inviter.id != hero.id && invited.id == hero.id;
    }
}
