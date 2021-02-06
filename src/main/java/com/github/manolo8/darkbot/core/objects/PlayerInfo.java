package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;
import eu.darkbot.api.objects.EntityInfo;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.core.manager.HeroManager.instance;

public class PlayerInfo extends Updatable implements EntityInfo {

    public int clanId;
    public int clanDiplomacy;
    public String clanTag = "";
    public String username = "";
    public int factionId;
    public int rank;
    public int gg;

    public boolean isEnemy() {
        if (getClanDiplomacy() == Diplomacy.NONE && getClanId() == 0 && getFaction() == Faction.NONE) return false;
        if (getClanDiplomacy() == Diplomacy.WAR) return true;

        return getClanDiplomacy() != Diplomacy.ALLIED
                && getClanDiplomacy() != Diplomacy.NOT_ATTACK_PACT
                && getFaction() != instance.getEntityInfo().getFaction();
    }

    @Override
    public void update() {
        clanId = readIntFromIntHolder(40);
        clanDiplomacy = readIntFromIntHolder(48);
        factionId = readIntFromIntHolder(72);
        rank = readIntFromIntHolder(80);
        gg = readIntFromIntHolder(88);
        if (username.isEmpty()) {
            clanTag = readStringFromStringHolder(56);
            username = readStringFromStringHolder(64);
        }
    }

    private int readIntFromIntHolder(int holderOffset) {
        return API.readMemoryInt(API.readMemoryLong(address + holderOffset) + 40);
    }

    private String readStringFromStringHolder(int holderOffset) {
        return API.readMemoryStringFallback(API.readMemoryLong(API.readMemoryLong(address + holderOffset) + 40), "");
    }

    @Override
    public Faction getFaction() {
        return Faction.of(factionId);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getClanTag() {
        return clanTag;
    }

    @Override
    public int getClanId() {
        return clanId;
    }

    @Override
    public Diplomacy getClanDiplomacy() {
        return Diplomacy.of(clanDiplomacy);
    }

    @Override
    public int getRankIconId() {
        return rank;
    }

    @Override
    public int getGalaxyRankIconId() {
        return gg;
    }

}
