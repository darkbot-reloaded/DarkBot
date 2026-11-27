package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.FlashListLong;
import it.unimi.dsi.fastutil.longs.LongCollection;

import static com.github.manolo8.darkbot.Main.API;

/**
 * Mediator of the action bar: knows the bar elements currently shown.
 * <p>
 * Nothing is read on update: the elements are only read when asked for, see {@link #readElements}.
 */
public class ItemsControlMenuMediator extends Updatable {

    /** Dictionary of the category bar's elements, element -> item. */
    private static final int CATEGORY_ELEMENTS = 0x50;
    /** Dictionary of the slot bars' elements, element -> slot. */
    private static final int SLOT_ELEMENTS = 0x60;

    private final FlashListLong categoryElements = FlashListLong.ofMapKeys();
    private final FlashListLong slotElements = FlashListLong.ofMapKeys();

    @Override
    public void update() {
    }

    /**
     * Adds the action bar elements shown now, of the category bar and the slot bars, to the collection.
     * @return false if they can't be read, e.g. while the bar isn't built: then nothing is added
     */
    public boolean readElements(LongCollection elements) {
        if (address == 0) return false;
        categoryElements.update(API.readLong(address + CATEGORY_ELEMENTS));
        slotElements.update(API.readLong(address + SLOT_ELEMENTS));
        if (!categoryElements.isReadable() || !slotElements.isReadable()) return false;

        elements.addAll(categoryElements);
        elements.addAll(slotElements);
        return true;
    }
}
