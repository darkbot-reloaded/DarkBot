package eu.darkbot.api.entities;

import eu.darkbot.api.entities.utils.EntityEffect;
import eu.darkbot.api.objects.Locatable;
import eu.darkbot.api.objects.LocationInfo;
import org.jetbrains.annotations.Nullable;

public interface Entity extends Locatable {
    /**
     * @return id of entity
     */
    int getId();

    /**
     * @return memory address
     */
    long getAddress();

    /**
     * Checks that entity is removed.
     * If (isRemoved() == true) then this entity wont be updated anymore.
     */
    boolean isRemoved();

    /**
     * @return {@link LocationInfo}
     */
    LocationInfo getLocationInfo();

    /**
     * Checks that entity have effect
     * @param effect to check
     * @return true if current entity have indicated effect
     */
    boolean hasEffect(int effect);

    default boolean hasEffect(EntityEffect effect) {
        return hasEffect(effect.getId());
    }

    /**
     * Sets metadata key with given value and stores it only for current entity.
     * Can be used for custom timers, checks, predicates etc. Make sure not to save
     * any custom class that wouldn't be able to be unloaded. Use java types.
     *
     * @param key your unique key
     * @param value to be put with your key
     * @return result of {@link java.util.Map#put(Object, Object)}
     */
    @Nullable
    Object setMetadata(String key, Object value);

    /**
     * Returns value associated with key or null if key doesnt exists.
     */
    @Nullable
    Object getMetadata(String key);

    /**
     * Clicks on entity only if distance is lower than 800.
     *
     * @param doubleClick should be double clicked
     * @return true on successful click
     */
    boolean clickOnEntity(boolean doubleClick);
}
