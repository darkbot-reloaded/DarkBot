package eu.darkbot.api.objects.slotbars;

import eu.darkbot.api.objects.Point;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface SlotBar extends Point {

    /**
     * @return true if slotbar is visible
     */
    boolean isVisible();

    /**
     * {@code slotIndex} should be in range from 0 to 9.
     *
     * @param slotIndex to get item from
     * @return item associated with given slot or null if none
     */
    @Nullable
    default Item getItem(int slotIndex) {
        return getSlots().get(slotIndex);
    }

    /**
     * Map of slots, range will be always from 0 to 9.
     * Item associated to slot can be null which means the slot is empty.
     *
     * @return map of slots
     */
    Map<Integer, @Nullable Item> getSlots();
}
