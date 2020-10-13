package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.core.manager.HeroManager.instance;

public class PlayerInfo extends Updatable {

    public int clanId;
    public int clanDiplomacy;
    public String clanTag = "";
    public String username = "";
    public int factionId;
    public int rank;
    public int gg;

    public boolean isEnemy() {
        return (clanDiplomacy != 0 || clanId != 0 || factionId != 0) && (factionId != instance.playerInfo.factionId && clanDiplomacy != 1 && clanDiplomacy != 2 || clanDiplomacy == 3);
    }

    @Override
    public void update() {
        clanId = readIntFromIntHolder(40);
        clanDiplomacy = readIntFromIntHolder(48);
        factionId = readIntFromIntHolder(72);
        rank = readIntFromIntHolder(80);
        gg = readIntFromIntHolder(88);
        clanTag = readStringFromStringHolder(56);
        username = readStringFromStringHolder(64);
    }

    private int readIntFromIntHolder(int holderOffset) {
        return API.readMemoryInt(API.readMemoryLong(address + holderOffset) + 40);
    }

    private String readStringFromStringHolder(int holderOffset) {
        return API.readMemoryStringFallback(API.readMemoryLong(API.readMemoryLong(address + holderOffset) + 40), "");
    }
}
