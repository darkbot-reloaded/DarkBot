package com.github.manolo8.darkbot.core.objects.slotbars;

import com.github.manolo8.darkbot.core.objects.Point;
import com.github.manolo8.darkbot.core.objects.facades.SlotBarsProxy;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

import static com.github.manolo8.darkbot.Main.API;

public class SlotBar extends MenuBar {
    private final CategoryBar categoryBar;
    private final SlotBarsProxy.Type slotType;
    public boolean isVisible;
    public Point stickOffset = new Point();
    public FlashList<Slot> slots = FlashList.ofVector(Slot::new);

    public SlotBar(CategoryBar categoryBar, SlotBarsProxy.Type type) {
        this.categoryBar = categoryBar;
        this.slotType = type;
    }

    @Override
    public void update() {
        if (address == 0) return;
        super.update();

        this.isVisible = API.readMemoryBoolean(address + 56);
        this.stickOffset.update(API.readMemoryLong(address + 72));

        this.slots.update(API.readMemoryLong(address + 64));
    }

    public class Slot extends Auto {
        public int slotNumber;
        public boolean premium; //not sure
        public String slotBarId;
        public @Nullable Item item;

        public @Nullable Item categoryItem;

        /*public Item getItem() {
            return item.address == 0 ? null : item;
        }*/

        @Override
        public void update() {
            this.slotNumber = API.readMemoryInt(address + 32);
            this.premium = API.readMemoryBoolean(address + 36);
            this.slotBarId = API.readString(address, 48);
            long itemPtr = API.readMemoryLong(address + 40);

            if (itemPtr == 0 && item != null) {
                editAndGetItem(item.id, i -> i.removeSlot(slotType, slotNumber));

                this.item = null;
                this.categoryItem = null;
            } else if (itemPtr != 0 && item == null) {
                this.item = new Item();
                item.update(itemPtr);

                categoryItem = editAndGetItem(item.id, i -> i.addSlot(slotType, slotNumber));
            } else if (itemPtr != 0 && itemPtr != item.address) {
                editAndGetItem(item.id, i -> i.removeSlot(slotType, slotNumber));

                item.update(itemPtr);
                categoryItem = editAndGetItem(item.id, i -> i.addSlot(slotType, slotNumber));
            } else if (item != null) item.update();
        }

        private Item editAndGetItem(String itemId, Consumer<Item> consumer) {
            Item item = categoryBar.categories.stream()
                    .flatMap(category -> category.items.stream())
                    .filter(i -> Objects.equals(i.id, itemId))
                    .findAny().orElse(null);

            if (item != null) consumer.accept(item);
            return item;
        }
    }
}
