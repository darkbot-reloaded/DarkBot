package com.github.manolo8.darkbot.core.objects.slotbars;

import com.github.manolo8.darkbot.core.itf.UpdatableAuto;

import static com.github.manolo8.darkbot.Main.API;

public class Item extends UpdatableAuto {
    public boolean selected;
    public String id, counterType, actionStyle, iconLootId;

    @Override
    public void update(long address) {
        if (address == 0) return;
        if (this.address != address) {
            this.id          = API.readMemoryString(address, 64);
            this.counterType = API.readMemoryString(address, 72);
            this.actionStyle = API.readMemoryString(address, 80);
            this.iconLootId  = API.readMemoryString(address, 96);
        }
        super.update(address);
    }

    @Override
    public void update() {
        this.selected    = API.readMemoryBoolean(address + 44);
    }
}
