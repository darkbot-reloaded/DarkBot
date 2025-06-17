package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import eu.darkbot.api.game.other.EntityInfo;

import static com.github.manolo8.darkbot.Main.API;

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
                && getFaction() != Main.INSTANCE.hero.getEntityInfo().getFaction();
    }

    @Override
    public void update() {
        clanId = readBindableInt(40);
        clanDiplomacy = readBindableInt(48);
        factionId = readBindableInt(72);
        rank = readBindableInt(80);
        gg = readBindableInt(88);
        if (username.isEmpty()) {
            clanTag = readBindableString("", 56);
            username = readBindableString("", 64);
        }
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
}
