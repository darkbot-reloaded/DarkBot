package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.def.Updatable;

import static com.github.manolo8.darkbot.Main.API;

public class PlayerInfo implements Updatable {

    private long address;

    public int clanId;
    public int clanDiplomacy;
    public String clanTag;
    public String username;
    public int factionId;
    public int rank;
    public int gg;

    public PlayerInfo() {
    }

    public PlayerInfo(long address) {
        update(address);
    }

    @Override
    public void update() {

        clanId = API.readMemoryInt(API.readMemoryLong(address + 40) + 40);
        clanDiplomacy = API.readMemoryInt(API.readMemoryLong(address + 48) + 40);
        clanTag = API.readMemoryString(API.readMemoryLong(API.readMemoryLong(address + 56) + 40));
        username = API.readMemoryString(API.readMemoryLong(API.readMemoryLong(address + 64) + 40));
        factionId = API.readMemoryInt(API.readMemoryLong(address + 72) + 40);
        rank = API.readMemoryInt(API.readMemoryLong(address + 80) + 40);
        gg = API.readMemoryInt(API.readMemoryLong(address + 88) + 40);
    }

    @Override
    public void update(long address) {
        this.address = address;
        update();
    }

    @Override
    public String toString() {
        return "PlayerInfo{" +
                "address=" + address +
                ", clanId=" + clanId +
                ", clanDiplomacy=" + clanDiplomacy +
                ", clanTag='" + clanTag + '\'' +
                ", username='" + username + '\'' +
                ", factionId=" + factionId +
                ", rank=" + rank +
                ", gg=" + gg +
                '}';
    }
}
