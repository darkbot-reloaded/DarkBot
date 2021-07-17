package eu.darkbot.api.game.entities;

import eu.darkbot.api.game.enums.EntityEffect;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.game.other.LocationInfo;
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
     * Is {@link Entity} valid and being updated, or has it disappeared client-side
     * If this method returns {@code false} the {@link Entity} should be discarded.
     *
     * @return true if {@link Entity} is valid and being updated, false otherwise
     */
    boolean isValid();

    /**
     * @return true if {@link Entity} can be selected (locked).
     */
    boolean isSelectable();

    /**
     * Selects this entity as the target in-game, can instantly attempt to attack with {@code tryAttack} param.
     *
     * @param tryAttack instantly attempt to attack this entity
     * @return true on successful selection
     */
    boolean trySelect(boolean tryAttack);

    /**
     * @return the location of the entity as well as other information about the location
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
     * @param effect The effect id to check
     * @return true if current entity has the effect, false otherwise
     */
    default boolean hasEffect(int effect) {
        return getEffects().contains(effect);
    }

    /**
     * Checks that {@link Entity} have given {@link EntityEffect}.
     *
     * @param entityEffect The effect to check
     * @return true if current entity has the effect, false otherwise
     */
    default boolean hasEffect(@NotNull EntityEffect entityEffect) {
        return hasEffect(entityEffect.getId());
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
