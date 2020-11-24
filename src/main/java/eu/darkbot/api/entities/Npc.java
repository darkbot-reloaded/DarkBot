package eu.darkbot.api.entities;

import eu.darkbot.api.entities.utils.Ammo;

import java.util.Optional;

public interface Npc extends Ship {

    /**
     * @return id of the npc
     */
    int getNpcId();

    /**
     * @return {@link Info} with some settings for {@link Npc}
     */
    Info getInfo();

    /**
     * Predefined settings for {@link Npc} customized by user.
     */
    interface Info {

        int getPriority();
        double getRadius();

        Optional<Ammo.Laser> getAttackAmmo();
        Optional<Ship.Formation> getAttackFormation();

        boolean hasExtraFlag(String flagId);
        boolean hasExtraFlag(ExtraFlag flag);

        void setExtraFlag(String flagId, boolean active);
        void setExtraFlag(ExtraFlag flag, boolean active);
    }

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
