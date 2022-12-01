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
        if (!isInvalid() && enabled)
            API.callMethodAsync(8, address);
    }

    @Deprecated
    public void toggle(boolean clickable) {
//        if (clickable) reset();
//        else setRadius(0);
    }

    @Deprecated
    public void setPriority(int priority) {
//        if (this.priority == priority || isInvalid()) return;
//        if (defPriority == -1) this.defPriority = this.priority;
//        API.replaceInt(address + 44, this.priority, this.priority = priority);
    }

    @Deprecated
    public void setRadius(int radius) {
//        if (this.radius == radius || isInvalid()) return;
//        if (defRadius == -1) this.defRadius = this.radius;
//        if (defRadius <= 0) return;
//        API.replaceInt(address + 40, this.radius, this.radius = radius);
    }

    @Deprecated
    public void reset() {
//        if (isInvalid()) return;
//        if (defRadius != radius)
//            API.replaceInt(address + 40, radius, radius = defRadius);
//        if (defPriority != priority)
//            API.replaceInt(address + 44, priority, priority = defPriority);
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
//        int oldRad = radius, oldPri = priority;
//        int newRadius = API.readMemoryInt(address + 40);
//
//        this.priority = API.readMemoryInt(address + 44);
//        this.enabled = API.readMemoryBoolean(address, 64, 32);
//
//        if (newRadius < 1000) {
//            radius = newRadius;
//            if (radius != oldRad) {
//                if (oldRad != defRadius) defRadius = radius;
//                setRadius(oldRad);
//            }
//        }
//        if (priority != oldPri) {
//            if (oldPri != defPriority) defPriority = priority;
//            setPriority(oldPri);
//        }
    }

    @Override
    public void update(long address) {
        super.update(address);
        if (address == 0) return;
        this.radius = defRadius = API.readMemoryInt(address + 40);
        this.priority = defPriority = API.readMemoryInt(address + 44);
    }
}
