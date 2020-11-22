package eu.darkbot.api.entities;

import eu.darkbot.api.objects.Locatable;
import eu.darkbot.api.objects.LocationInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

/**
 * Generic entity object in-game.
 */
public interface Entity extends Locatable {

    /**
     * @return id of the {@link Entity}
     */
    int getId();

    /**
     * @return memory address of {@link Entity} object
     */
    long getAddress();

    /**
     * Checks that entity is removed.
     * If (isRemoved() == true) then this entity wont be updated anymore.
     */
    boolean isRemoved();

    /**
     * @return true if {@link Entity} can be clicked.
     */
    boolean isClickable();

    /**
     * Clicks on entity only if distance to {@link eu.darkbot.api.managers.HeroAPI} is lower than 800.
     *
     * @param doubleClick should be double clicked
     * @return true on successful click
     */
    boolean tryMouseClick(boolean doubleClick);

    /**
     * @return {@link LocationInfo}
     */
    LocationInfo getLocationInfo();

    /**
     * Checks that {@link Entity} have given effect id.
     *
     * @param effect to check
     * @return true if current entity have indicated effect
     */
    default boolean hasEffect(int effect) {
        return getEffects().contains(effect);
    }

    default boolean hasEffect(@NotNull Entity.Effect effect) {
        return hasEffect(effect.getId());
    }

    /**
     * @return {@link Collection} of currently owned effects ids
     */
    Collection<Integer> getEffects();

    /**
     * Sets metadata key with given value and stores it only for current entity.
     * Can be used for custom timers, checks, predicates etc. Make sure not to save
     * any custom class that wouldn't be able to be unloaded.
     * <p>
     * <b>Use java types!</b>
     *
     * @param key   your unique key
     * @param value to be put with your key
     */
    void setMetadata(String key, Object value);

    /**
     * Returns value associated with key or {@link Optional#empty()} if key doesnt exists.
     */
    Optional<Object> getMetadata(String key);

    /**
     * Represents in-game entity effects.
     */
    // TODO: 01.11.2020 add more effects
    enum Effect {
        UNDEFINED(-1),
        LOCATOR(1),
        PET_SPAWN(2),
        ENERGY_LEECH(11),
        NPC_ISH(16),
        DRAW_FIRE(36),
        ISH(84),
        STICKY_BOMB(56),
        POLARITY_POSITIVE(65),
        POLARITY_NEGATIVE(66),
        INFECTION(85);

        private final int id;

        Effect(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
