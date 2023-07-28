package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Updatable;

import static com.github.manolo8.darkbot.Main.API;

public class Clickable extends Updatable {

    public int radius;
    public int priority;
    public boolean enabled;

    public int defRadius = -1;
    public int defPriority = -1;

    public void click() {
        if (!isInvalid() && enabled) {
            // size <= 128 = normal click trait, > 128 = portal/battlestation/etc click trait
            short instanceSize = (short) API.readInt(address, 0x10, 0x28, 0xF0);
            if (instanceSize > 128) API.callMethodChecked(false, "23(26)0086311000", 8, address);
            else API.callMethodChecked(false, "23(26)008531900", 8, address);
        }
    }

    @Deprecated
    public void setPriority(int priority) {
    }

    @Deprecated
    public void setRadius(int radius) {
    }

    @Deprecated
    public void reset() {
    }

    /**
     * @return prevent swf crash
     */
    public boolean isInvalid() {
        return defRadius <= 0 || address == 0 || API.readMemoryLong(address) != BotInstaller.SCRIPT_OBJECT_VTABLE;
    }

    @Override
    public void update() {
        if (address == 0 || API.readMemoryLong(address) != BotInstaller.SCRIPT_OBJECT_VTABLE) return;

        this.radius = this.defRadius = API.readMemoryInt(address + 40);
        this.priority = this.defPriority = API.readMemoryInt(address + 44);
        this.enabled = API.readMemoryBoolean(address, 64, 32);
    }

    @Override
    public void update(long address) {
        super.update(address);
        if (address == 0) return;
        this.radius = defRadius = API.readMemoryInt(address + 40);
        this.priority = defPriority = API.readMemoryInt(address + 44);
    }
}
