package eu.darkbot.api.objects.slotbars;

import eu.darkbot.api.managers.SlotBarAPI;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface Item {

    /**
     * @return true if {@link Item} has shortcut on any of the slot bar listed below:
     * <p>
     * {@link SlotBarAPI.Type#DEFAULT_BAR}
     * <p>
     * {@link SlotBarAPI.Type#PREMIUM_BAR}
     * <p>
     * {@link SlotBarAPI.Type#PRO_ACTION_BAR}
     */
    boolean hasShortcut();

    /**
     * This method returns first slot bar which {@link Item} is associated as {@link Map.Entry}
     * Where {@link Map.Entry#getKey()} is {@link SlotBarAPI.Type} and {@link Map.Entry#getValue()} is slot number.
     * <p>
     *
     * @return first slot entry associated with {@link Item} otherwise null
     * @apiNote this method returns first available slot entry in order:
     * <p>
     * {@link SlotBarAPI.Type#DEFAULT_BAR} then
     * <p>
     * {@link SlotBarAPI.Type#PREMIUM_BAR} then
     * <p>
     * {@link SlotBarAPI.Type#PRO_ACTION_BAR} if none returns {@code null}
     */
    @Nullable
    Map.Entry<SlotBarAPI.Type, Integer> getSlotEntry();

    /**
     * @return id of the item
     */
    String getId();

    /**
     * @return current quantity of item
     */
    double getQuantity();

    /**
     * @return true if item is selected
     */
    boolean isSelected();

    /**
     * @return true if item can be bought via click
     */
    boolean isBuyable();

    /**
     * @return true if item can be activated
     */
    boolean isActivatable();

    /**
     * @return true if item is available and is not greyed out
     */
    boolean isAvailable();

    /**
     * @return true if item is ready, available and can be clicked
     */
    boolean isReady();

    /**
     * @return time in {@code milliseconds} needed to be passed till {@link Item} will be available
     */
    double readyIn();

    /**
     * @return total cooldown time in {@code milliseconds} of {@link Item}
     */
    double totalCooldown();
}
