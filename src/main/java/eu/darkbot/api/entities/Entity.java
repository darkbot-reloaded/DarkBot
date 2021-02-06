package eu.darkbot.api.entities;

import eu.darkbot.api.entities.other.Effect;
import eu.darkbot.api.objects.Locatable;
import eu.darkbot.api.objects.LocationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Generic entity object in-game.
 */
public interface Entity extends Locatable {

    /**
     * @return id of the {@link Entity}
     */
    int getId();

    /**
     * Is {@link Entity} valid and being updated, //(?out of map?).
     * If that method returns {@code false} dont use this {@link Entity} anymore,
     * values etc. wont be updated.
     *
     * @return true if {@link Entity} is valid and being updated
     */
    boolean isValid();

    /**
     * @return true if {@link Entity} can be selected.
     */
    boolean isSelectable();

    /**
     * Selects this entity as the target in-game, can instantly attempt to attack with {@code tryAttack} param.
     *
     * @param tryAttack instant attempt to attack this entity
     * @return true on successful selection
     */
    boolean trySelect(boolean tryAttack);

    /**
     * @return {@link LocationInfo}
     */
    LocationInfo getLocationInfo();

    @Override
    default double getX() {
        return getLocationInfo().getX();
    }

    @Override
    default double getY() {
        return getLocationInfo().getY();
    }

    /**
     * Checks that {@link Entity} have given effect id.
     *
     * @param effect to check
     * @return true if current entity have indicated effect
     */
    default boolean hasEffect(int effect) {
        return getEffects().contains(effect);
    }

    default boolean hasEffect(@NotNull Effect effect) {
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
     * Returns value associated with key or {@code null} if key doesnt exists.
     */
    @Nullable Object getMetadata(String key);
}
