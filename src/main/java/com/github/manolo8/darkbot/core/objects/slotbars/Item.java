package com.github.manolo8.darkbot.core.objects.slotbars;

import com.github.manolo8.darkbot.core.itf.UpdatableAuto;

import static com.github.manolo8.darkbot.Main.API;

public class Item extends UpdatableAuto {
    // Use it only if isReady() == false
    public final ItemTimer itemTimer = new ItemTimer();

    public double quantity;
    public boolean selected, buyable, activatable, available, visible;
    public String id, counterType, actionStyle, iconLootId;

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
        if (address == 0) return;
        if (this.address != address) {
            this.id          = API.readMemoryString(address, 64);
            this.counterType = API.readMemoryString(address, 72);
            this.actionStyle = API.readMemoryString(address, 80);
            this.iconLootId  = API.readMemoryString(address, 96);

            //this.itemTimer.update(API.readMemoryLong(address, 88, 40)); sometimes keeps old address
        }
        super.update(address);
    }

    public boolean isReady() {
        return this.itemTimer.address == 0;
    }

    //Object doesnt exist on "ready" state
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
