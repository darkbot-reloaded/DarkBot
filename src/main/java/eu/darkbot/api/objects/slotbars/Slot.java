package eu.darkbot.api.objects.slotbars;

import com.github.manolo8.darkbot.core.objects.slotbars.Item;
import eu.darkbot.api.managers.SlotBarAPI;
import org.jetbrains.annotations.NotNull;

public interface Slot {

    static Slot of(int slotNumber, @NotNull SlotBarAPI.Type slotBarType) {
        return new Item.Slot(slotNumber, slotBarType);
    }

    int getSlotNumber();

    SlotBarAPI.Type getSlotBarType();
}
