package eu.darkbot.api.objects.group;

import eu.darkbot.api.objects.Location;

public interface GroupMember extends PartialGroupMember {
    Location getLocation();

    MemberInfo getMemberInfo();
    MemberInfo getTargetInfo();

    int getFactionId();
    int getLevel();
    int getMapId();
    boolean getIsAttacked();
    boolean getIsCloaked();
    boolean getIsLeader();
    boolean getIsDead();
    boolean getIsLocked(); // is selected by hero
}
