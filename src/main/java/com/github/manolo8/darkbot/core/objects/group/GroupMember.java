package com.github.manolo8.darkbot.core.objects.group;

import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.utils.Location;
import eu.darkbot.api.objects.EntityInfo;

import static com.github.manolo8.darkbot.Main.API;

public class GroupMember extends PartialGroupMember implements eu.darkbot.api.objects.group.GroupMember {
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

    @Override
    public eu.darkbot.api.objects.Location getLocation() {
        return location;
    }

    @Override
    public eu.darkbot.api.objects.group.MemberInfo getMemberInfo() {
        return memberInfo;
    }

    @Override
    public eu.darkbot.api.objects.group.MemberInfo getTargetInfo() {
        return targetInfo;
    }

    @Override
    public EntityInfo.Faction getFactionId() {
        return EntityInfo.Faction.of(factionId);
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getMapId() {
        return mapId;
    }

    @Override
    public boolean isAttacked() {
        return isAttacked;
    }

    @Override
    public boolean isCloaked() {
        return isCloacked;
    }

    @Override
    public boolean isLeader() {
        return isLeader;
    }

    @Override
    public boolean isDead() {
        return isDead;
    }

    @Override
    public boolean isLocked() {
        return isLocked;
    }
}
