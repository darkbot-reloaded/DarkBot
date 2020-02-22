package com.github.manolo8.darkbot.core.objects.swf.group;

import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.utils.Location;

import static com.github.manolo8.darkbot.Main.API;

public class GroupMember extends PartialGroupMember {
    public Location location = new Location();
    public MemberInfo memberInfo = new MemberInfo();
    public MemberInfo targetInfo = new MemberInfo();

    public int factionId;
    public int level;
    public int mapId;
    public boolean isAttacked;
    public boolean isCloacked;
    public boolean isLeader;
    public boolean isDead;
    public boolean isLocked; // is selected by hero

    public String getMap() {
        return StarManager.getInstance().byId(mapId).shortName;
    }

    public String getDisplayText(boolean hideUsername) {
        return getMap() + " " + (hideUsername ? "-Hidden-" : getUsername()) + " ";
    }

    @Override
    public void update() {
        location.set(API.readMemoryInt(address + 0x38), API.readMemoryInt(address + 0x3C));
        memberInfo.update(API.readMemoryLong(address + 0x78));
        targetInfo.update(API.readMemoryLong(address + 0x80));

        factionId  = API.readMemoryInt(address + 0x24);
        level      = API.readMemoryInt(address + 0x28);
        mapId      = API.readMemoryInt(address + 0x34);
        isAttacked = API.readMemoryBoolean(address + 0x44);
        isCloacked = API.readMemoryBoolean(address + 0x48);
        isLeader   = API.readMemoryBoolean(address + 0x50);
        isDead     = API.readMemoryBoolean(address + 0x54);
        isLocked   = API.readMemoryBoolean(address + 0x60);

        super.update();
    }
}
