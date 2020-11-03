package eu.darkbot.api.objects.group;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Group {
    List<GroupMember> getMembers();
    GroupMember getSelectedMember();

    int getId();
    int getSize();
    int getMaxSize();
    boolean isOpen(); // if the group is open to allowing anyone to invite
    boolean isLeader();

    /**
     * @return if user is currently in group or not
     */
    default boolean isValid() {
        return getId() != 0 && getSize() != 0 && getMaxSize() == 8;
    }

    /**
     * @param id  the id of the group member you want to get
     * @return the <code>GroupMember</code> that matches with the <code>id</code> else null
     */
    @Nullable
    default GroupMember getMember(int id) {
        for (GroupMember member : getMembers()) {
            if (member.getId() == id) return member;
        }
        return null;
    }

    /**
     * @param member  the <code>GroupMember</code> that you want the index of
     * @return the index of the <code>member</code> else -1 if not found
     */
    default int indexOf(GroupMember member) {
        for (int i = 0; i < getSize(); i++)
            if (getMembers().get(i) == member) return i;
        return -1;
    }

    interface Invite {
        PartialGroupMember getInviter();
        PartialGroupMember getInvited();

        boolean isIncoming();
        boolean isValid();
    }
}
