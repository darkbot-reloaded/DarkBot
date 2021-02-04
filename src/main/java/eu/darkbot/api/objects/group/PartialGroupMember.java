package eu.darkbot.api.objects.group;

/**
 * Group members that are currently not part of the group yet,
 * but are waiting for their invite to be accepted or declined.
 */
public interface PartialGroupMember {
    int getId();
    String getUsername();
}
