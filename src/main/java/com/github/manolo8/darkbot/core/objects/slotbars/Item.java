package com.github.manolo8.darkbot.core.objects.slotbars;

import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.facades.SlotBarsProxy;

import java.util.EnumMap;
import java.util.Map;

import static com.github.manolo8.darkbot.Main.API;

public class Item extends UpdatableAuto implements eu.darkbot.api.objects.Item {
    // Only has relevant info if !isReady()
    public final ItemTimer itemTimer = new ItemTimer();
    private final Map<SlotBarsProxy.Type, Slot> associatedSlots = new EnumMap<>(SlotBarsProxy.Type.class);

    public double quantity;
    public boolean selected, buyable, activatable, available, visible;
    public String id, counterType, actionStyle, iconLootId;

    void removeSlot(SlotBarsProxy.Type slotType) {
        this.associatedSlots.remove(slotType);
    }

    void addSlot(SlotBarsProxy.Type slotType, int slotNumber) {
        this.associatedSlots.put(slotType, new Slot(slotNumber, slotType));
    }

    @Override
    public void update() {
        this.buyable = API.readMemoryBoolean(address + 36);
        this.activatable = API.readMemoryBoolean(address + 40);
        this.selected = API.readMemoryBoolean(address + 44);
        this.available = API.readMemoryBoolean(address + 48);
        this.visible = API.readMemoryBoolean(address + 52);
        //this.blocked   = API.readMemoryBoolean(address + 56); // doesnt work
        this.quantity = API.readMemoryDouble(address + 128);

        long tempAddr = API.readMemoryLong(address, 88, 40);
        if (itemTimer.address != tempAddr) this.itemTimer.update(tempAddr);
        this.itemTimer.update();
    }

    @Override
    public void update(long address) {
        if (this.address != address) {
            this.id          = API.readMemoryString(address, 64);
            this.counterType = API.readMemoryString(address, 72);
            this.actionStyle = API.readMemoryString(address, 80);
            this.iconLootId  = API.readMemoryString(address, 96);
        }
        super.update(address);
    }

    public boolean hasShortcut() {
        return !associatedSlots.isEmpty();
    }

    public Slot getSlot() {
        return this.associatedSlots.entrySet().stream()
                .findFirst()
                .map(Map.Entry::getValue).orElse(null);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public double getQuantity() {
        return quantity;
    }

    @Override
    public boolean isActivatable() {
        return activatable && hasShortcut();
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public boolean isBuyable() {
        return buyable;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public boolean isReady() {
        return itemTimer.address == 0;
    }

    @Override
    public double readyIn() {
        return itemTimer.availableIn;
    }

    @Override
    public double totalCooldown() {
        return itemTimer.itemDelay;
    }

    @Override
    public String toString() {
        return "Item{" +
                "isReady=" + isReady() +
                ", associatedSlots=" + associatedSlots +
                ", quantity=" + quantity +
                ", selected=" + selected +
                ", buyable=" + buyable +
                ", activatable=" + activatable +
                ", available=" + available +
                ", visible=" + visible +
                ", id='" + id + '\'' +
                '}';
    }

    public static class Slot {
        public final int slotNumber;
        public final SlotBarsProxy.Type slotBarType;

        public Slot(int slotNumber, SlotBarsProxy.Type slotBarType) {
            this.slotNumber = slotNumber;
            this.slotBarType = slotBarType;
        }
    }

    public static class ItemTimer extends UpdatableAuto {
        public double elapsed, startTime, itemDelay, availableIn;

        @Override
        public void update() {
            if (address == 0) return;

            this.elapsed = API.readMemoryDouble(address + 72);
            this.availableIn = API.readMemoryDouble(address + 96);
        }

        @Override
        public void update(long address) {
            this.address = address;
            if (address == 0) return;

            this.startTime = API.readMemoryDouble(address + 80);
            this.itemDelay = API.readMemoryDouble(address + 88);
        }

        @Override
        public String toString() {
            return "ItemTimer{" +
                    "elapsed=" + elapsed +
                    ", startTime=" + startTime +
                    ", itemDelay=" + itemDelay +
                    ", availableIn=" + availableIn +
                    '}';
        }
    }
}
