package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.game.entities.Ship;
import eu.darkbot.api.game.group.GroupMember;
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
     * Numerical id for the group you are currently in
     * @return id of the group if you are in one, 0 otherwise
     */
    int getId();

    /**
     * Size of the current group, including yourself.
     * In a group of you and one other player, the size is 2.
     *
     * @return size of the group if in one, 0 otherwise
     */
    int getSize();

    /**
     * @return The maximum size of groups, currently in-game 8
     */
    int getMaxSize();

    /**
     * @return if group is open and anyone can invite other players
     */
    boolean isOpen();

    /**
     * @return if you are the leader of your group
     */
    boolean isLeader();

    /**
     * If all conditions to be able to invite players are met, for example:
     *  - You're not in a group
     *  - You are the leader of the group or invites are open
     *  - The group isn't full
     *
     * @return if able to invite players at this time, false otherwise
     */
    boolean canInvite();

    /**
     * The given list will stay updated whenever members are added or
     * removed, however, you cannot modify this list yourself.
     * Works similar to collections provided in {@link EntitiesAPI}
     *
     * @return list of all other players in the group, excluding yourself.
     */
    List<? extends GroupMember> getMembers();

    /**
     * If you lock a player in-game on the space map, in the outfit
     * window they become selected (appear highlighted)
     *
     * @return The currently selected member in outfit window, null if none is selected
     */
    @Nullable GroupMember getSelectedMember();

    /**
     * Find a group member by their user id
     * @param id the user id to search for
     * @return the group member if found, null otherwise.
     */
    @Nullable GroupMember getMember(int id);

    /**
     * Find a group member by their ship entity
     * @param ship The player ship entity to search for
     * @return the group member if found, null otherwise.
     */
    @Nullable
    default GroupMember getMember(Ship ship) {
        return getMember(ship.getId());
    }

    /**
     * @return The list of member invites that are currently pending
     */
    List<? extends GroupMember.Invite> getInvites();

    /*
    TODO: These need to be properly implemented, in a way that will queue operations,
          otherwise this will be very prone to failing.
          Also consider maybe not having an API that works via direct method calls, and instead
          works by assigning tags to players. Although it may be inconsistent with how many other
          things operate in the bot.

    void kickMember(int id);

    default void kickMember(GroupMember member) {
        kickMember(member.getId());
    }

    default void kickMember(Ship ship) {
        kickMember(ship.getId());
    }

    void sendInvite(String username);
    void acceptInvite(GroupMember.Invite invite);
    */

}
