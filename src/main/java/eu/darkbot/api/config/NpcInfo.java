package eu.darkbot.api.config;

import eu.darkbot.api.entities.Npc;
import eu.darkbot.api.entities.other.SelectableItem;

import java.util.Optional;

/**
 * Predefined settings for {@link Npc} customized by user.
 */
public interface NpcInfo {

    int getPriority();
    double getRadius();

    Optional<SelectableItem.Laser> getAmmo();
    Optional<SelectableItem.Formation> getFormation();

    boolean hasExtraFlag(ExtraFlag flag);
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
