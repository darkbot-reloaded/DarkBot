package eu.darkbot.api.objects.slotbars;

import eu.darkbot.api.objects.Point;
import org.jetbrains.annotations.Nullable;

public interface SlotBar extends Point {

    boolean isVisible();

    @Nullable
    Item getSlot(int slotIndex);

    Item[] getSlots();
}
