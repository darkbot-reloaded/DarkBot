package com.github.manolo8.darkbot.core.objects.slotbars;

import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.facades.SlotBarsProxy;

import java.util.*;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import static com.github.manolo8.darkbot.Main.API;

public class Item extends UpdatableAuto implements eu.darkbot.api.items.Item {
    private static final int START = 36, END = 128 + 8;

    private static final byte[] BUFFER = new byte[END - START];
    // Only has relevant info if !isReady()
    public final ItemTimer itemTimer = new ItemTimer();
    private final Map<SlotBarsProxy.Type, Set<Integer>> associatedSlots = new EnumMap<>(SlotBarsProxy.Type.class);

    public double quantity;
    public boolean selected, buyable, activatable, available, visible;
    public String id, counterType, actionStyle, iconLootId;

    void removeSlot(SlotBarsProxy.Type slotType, int slotNumber) {
        getShortcutList(slotType).remove(slotNumber);
    }

    void addSlot(SlotBarsProxy.Type slotType, int slotNumber) {
        getShortcutList(slotType).add(slotNumber);
    }

    private Set<Integer> getShortcutList(SlotBarsProxy.Type type) {
        return this.associatedSlots.computeIfAbsent(type, l -> new HashSet<>());
    }

    @Override
    public void update() {
        // There are *a lot* of items in-game
        // Doing 5 boolean-read calls is way more expensive than a single mem-read to the buffer
        // This IS very ugly, but improves performance.
        // We also avoid updating timer if no other flags change for the extra 3 long-read calls
        API.readMemory(address + START, BUFFER);

        boolean buyable     = BUFFER[0] == 1;
        boolean activatable = BUFFER[4] == 1;
        boolean selected = BUFFER[8] == 1;
        boolean available = BUFFER[12] == 1;
        boolean visible = BUFFER[16] == 1;
        double quantity = ByteUtils.getDouble(BUFFER, 92);

        if (this.buyable != buyable ||
                this.activatable != activatable ||
                this.selected != selected ||
                this.available != available ||
                this.visible != visible ||
                this.quantity != quantity) {
            this.buyable = buyable;
            this.activatable = activatable;
            this.selected = selected;
            this.available = available;
            this.visible = visible;
            this.quantity = quantity;

            long timerAddr = API.readMemoryLong(address, 88, 40);
            if (itemTimer.address != timerAddr) this.itemTimer.update(timerAddr);
            this.itemTimer.update();
        }
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
        return getSlotBarType() != null;
    }

    public SlotBarsProxy.Type getSlotBarType() {
        return this.associatedSlots.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    public int getFirstSlotNumber() {
        SlotBarsProxy.Type slotBarType = getSlotBarType();
        if (slotBarType == null) return -1;

        return getShortcutList(slotBarType).stream()
                .findFirst().orElse(-1);
    }

    public boolean containsSlotNumber(int slotNumber) {
        SlotBarsProxy.Type slotBarType = getSlotBarType();
        if (slotBarType == null) return false;

        return getShortcutList(slotBarType).contains(slotNumber == 0 ? 10 : slotNumber);
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
    public boolean isUsable() {
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
