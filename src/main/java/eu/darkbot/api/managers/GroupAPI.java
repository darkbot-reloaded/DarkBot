package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.objects.group.Group;
import eu.darkbot.api.objects.group.GroupMember;

import java.util.List;

/**
 * API for group
 */
public interface GroupAPI extends API {

    Group getGroup();
    boolean canPing();
    List<Group.Invite> getInvites();

    boolean canKick();
    boolean canInvite();
    boolean canOpenInvites();

    void kick(int id);
    void kick(GroupMember member);

    void sendInvite(String username);
    void acceptInvite(Group.Invite invite);
    void openInvites();

    void tryAcceptInvites();
    void trySendInvites();
}
