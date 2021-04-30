package eu.darkbot.api.config;

import eu.darkbot.api.entities.Npc;
import eu.darkbot.api.entities.other.SelectableItem;

import java.util.Optional;

/**
 * Predefined settings for {@link Npc} customized by user.
 */
public interface NpcInfo {

    /**
     * @return How important this box is to the user, the lower number is more important
     */
    int getPriority();

    /**
     * @return How far away the user wants to stand from this npc, in in-game distance units
     */
    double getRadius();

    /**
     * @return The type of ammo the user wants to attack this npc with.
     */
    Optional<SelectableItem.Laser> getAmmo();

    /**
     * @return The formation the user wants to use for this npc.
     */
    Optional<SelectableItem.Formation> getFormation();

    /**
     * If the user has selected the additional configuration flag.
     * @param flag The flag to check
     * @return If the user has selected this flag or not
     */
    boolean hasExtraFlag(ExtraFlag flag);

    /**
     * This will modify the configuration of the user, be extremely
     * cautious when using this as it can confuse users.
     *
     * @param flag The flag to set
     * @param active if the flag should be set to active or inactive
     */
    void setExtraFlag(ExtraFlag flag, boolean active);


    interface ExtraFlag {
        String getName();
        String getShortName();
        String getDescription();

        default String getKey() {
            return null;
        }

        default String getId() {
            return getClass().getCanonicalName() +
                    (getClass().isEnum() ? ((Enum<?>) this).name() : getName());
        }
    }
}
