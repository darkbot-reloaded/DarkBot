package com.github.manolo8.darkbot.core.objects.slotbars;

import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import eu.darkbot.api.managers.SlotBarAPI;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

import static com.github.manolo8.darkbot.Main.API;

public class Item extends UpdatableAuto implements eu.darkbot.api.objects.slotbars.Item {
    // Only has relevant info if !isReady()
    public final ItemTimer itemTimer = new ItemTimer();

    public double quantity;
    public boolean selected, buyable, activatable, available, visible;
    public String id, counterType, actionStyle, iconLootId;

    private final Map<SlotBarAPI.Type, Integer> associatedSlots = new EnumMap<>(SlotBarAPI.Type.class);

    void removeSlot(SlotBarAPI.Type slotType) {
        this.associatedSlots.remove(slotType);
    }

    void addSlot(SlotBarAPI.Type slotType, int slotNumber) {
        this.associatedSlots.put(slotType, slotNumber);
    }

    @Override
    public void update() {
        this.buyable     = API.readMemoryBoolean(address + 36);
        this.activatable = API.readMemoryBoolean(address + 40);
        this.selected    = API.readMemoryBoolean(address + 44);
        this.available   = API.readMemoryBoolean(address + 48);
        this.visible     = API.readMemoryBoolean(address + 52);
        //this.blocked   = API.readMemoryBoolean(address + 56); // doesnt work
        this.quantity    = API.readMemoryDouble(address + 128);

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

    @Override
    public boolean hasShortcut() {
        return !associatedSlots.isEmpty();
    }

    @Override
    public @Nullable Map.Entry<SlotBarAPI.Type, Integer> getSlotEntry() {
        return this.associatedSlots.entrySet().stream().findFirst().orElse(null);
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
    public boolean isSelected() {
        return selected;
    }

    @Override
    public boolean isBuyable() {
        return buyable;
    }

    @Override
    public boolean isActivatable() {
        return activatable;
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
    }
}
