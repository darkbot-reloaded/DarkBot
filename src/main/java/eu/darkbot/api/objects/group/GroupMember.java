package eu.darkbot.api.objects.group;

import eu.darkbot.api.objects.EntityInfo;
import eu.darkbot.api.objects.Location;

public interface GroupMember extends PartialGroupMember {
    Location getLocation();

    MemberInfo getMemberInfo();
    MemberInfo getTargetInfo();

    EntityInfo.Faction getFactionId();
    int getLevel();
    int getMapId();
    boolean isAttacked();
    boolean isCloaked();
    boolean isLeader();
    boolean isDead();
    boolean isLocked(); // is selected by hero

    interface Invite {
        PartialGroupMember getInviter();
        PartialGroupMember getInvited();

        boolean isIncoming();
        boolean isValid();
    }
}
