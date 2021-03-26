package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.Ship;
import eu.darkbot.api.objects.group.GroupMember;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * API for group
 */
public interface GroupAPI extends API.Singleton {
    /**
     * @return true if {@link HeroAPI} is in group.
     */
    boolean hasGroup();

    /**
     * @return group id otherwise 0
     */
    int getId();

    int getSize();

    int getMaxSize();

    /**
     * @return true if group is open and {@link HeroAPI} can invite
     */
    boolean isOpen();

    /**
     * @return true if {@link HeroAPI} is group's leader
     */
    boolean isLeader();

    List<? extends GroupMember> getMembers();

    /**
     * @return what is it exactly?
     */
    @Nullable GroupMember getSelectedMember();

    @Nullable GroupMember getMember(int id);

    @Nullable
    default GroupMember getMember(Ship ship) {
        return getMember(ship.getId());
    }

    void kickMember(int id);

    default void kickMember(GroupMember member) {
        kickMember(member.getId());
    }

    default void kickMember(Ship ship) {
        kickMember(ship.getId());
    }

    List<? extends GroupMember.Invite> getInvites();

    boolean canInvite();

    void sendInvite(String username);
    void acceptInvite(GroupMember.Invite invite);
}
