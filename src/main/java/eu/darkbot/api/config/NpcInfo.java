package eu.darkbot.api.config;

import eu.darkbot.api.entities.Npc;
import eu.darkbot.api.entities.other.Ammo;
import eu.darkbot.api.entities.other.Formation;

import java.util.Optional;

/**
 * Predefined settings for {@link Npc} customized by user.
 */
public interface NpcInfo {

    int getPriority();
    double getRadius();

    Optional<Ammo.Laser> getAmmo();
    Optional<Formation> getFormation();

    boolean hasExtraFlag(String flagId);
    boolean hasExtraFlag(ExtraFlag flag);

    void setExtraFlag(String flagId, boolean active);
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
