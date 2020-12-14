package com.github.manolo8.darkbot.core.objects.slotbars;

import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.Point;
import com.github.manolo8.darkbot.core.objects.facades.SlotBarsProxy;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import eu.darkbot.api.managers.HeroItemsAPI;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.github.manolo8.darkbot.Main.API;

public class SlotBar extends MenuBar {
    private final ObjArray slotsArr = ObjArray.ofVector(true);
    private final CategoryBar categoryBar;
    private final SlotBarsProxy.Type slotType;
    public boolean isVisible;
    public Point stickOffset = new Point();
    public List<Slot> slots = new ArrayList<>();

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

        this.slotsArr.update(API.readMemoryLong(address + 64));
        this.slotsArr.sync(slots, Slot::new, null);
    }

    public class Slot extends UpdatableAuto {
        public int slotNumber;
        public boolean premium; //not sure
        public String slotBarId;
        public @Nullable Item item;

        /*public Item getItem() {
            return item.address == 0 ? null : item;
        }*/

        @Override
        public void update() {
            this.slotNumber = API.readMemoryInt(address + 32);
            this.premium = API.readMemoryBoolean(address + 36);
            this.slotBarId = API.readMemoryString(address, 48);
            long itemPtr = API.readMemoryLong(address + 40);

            if (itemPtr == 0 && item != null) {
                editItem(item.id, i -> i.removeSlot(slotType));

                this.item = null;
            } else if (itemPtr != 0 && item == null) {
                this.item = new Item();
                item.update(itemPtr);

                editItem(item.id, i -> i.addSlot(slotType, slotNumber));
            } else if (itemPtr != 0 && itemPtr != item.address) {
                editItem(item.id, i -> i.removeSlot(slotType));

                item.update(itemPtr);
                editItem(item.id, i -> i.addSlot(slotType, slotNumber));
            } else if (item != null) item.update();
        }

        private void editItem(String itemId, Consumer<Item> consumer) {
            categoryBar.categories.stream()
                    .flatMap(category -> category.items.stream())
                    .filter(i -> i.id.equals(itemId))
                    .findAny()
                    .ifPresent(consumer);
        }
    }
}
