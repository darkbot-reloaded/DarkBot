package eu.darkbot.api.objects.group;

import eu.darkbot.api.objects.Location;

public interface GroupMember extends PartialGroupMember {
    Location getLocation();

    MemberInfo getMemberInfo();
    MemberInfo getTargetInfo();

    int getFactionId();
    int getLevel();
    int getMapId();
    boolean isAttacked();
    boolean isCloaked();
    boolean isLeader();
    boolean isDead();
    boolean isLocked(); // is selected by hero
}
