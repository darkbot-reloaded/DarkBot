package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;

import static com.github.manolo8.darkbot.Main.API;

public class Clickable extends Updatable {

    private long confirm;

    public int radius;
    public int priority;

    public int defRadius = -1;
    public int defPriority = -1;

    public void setPriority(int priority) {
        if (this.priority == priority || isInvalid()) return;
        if (defPriority == -1) this.defPriority = this.priority;
        API.replaceInt(address + 44, this.priority, this.priority = priority);
    }

    public void setRadius(int radius) {
        if (this.radius == radius || isInvalid()) return;
        if (defRadius == -1) this.defRadius = this.radius;
        if (defRadius <= 0) return;
        API.replaceInt(address + 40, this.radius, this.radius = radius);
    }

    public void reset() {
        if (isInvalid()) return;
        if (defRadius != -1 && defRadius != radius)
            API.replaceInt(address + 40, radius, radius = defRadius);
        if (defRadius != -1 && defPriority != priority)
            API.replaceInt(address + 44, priority, priority = defPriority);
    }

    /**
     * @return prevent swf crash
     */
    private boolean isInvalid() {
        return defRadius <= 0 || address == 0 || API.readMemoryLong(address) != confirm;
    }

    @Override
    public void update() {
        if (isInvalid()) return;
        int oldRad = radius, oldPri = priority;
        this.radius = API.readMemoryInt(address + 40);
        this.priority = API.readMemoryInt(address + 44);

        if (radius != oldRad) {
            if (oldRad != defRadius) defRadius = radius;
            setRadius(oldRad);
        }
        if (priority != oldPri) {
            if (oldPri != defPriority) defPriority = priority;
            setPriority(oldPri);
        }
    }

    @Override
    public void update(long address) {
        super.update(address);
        if (address == 0) return;
        this.confirm = API.readMemoryLong(address);
        this.radius = defRadius = API.readMemoryInt(address + 40);
        this.priority = defPriority = API.readMemoryInt(address + 44);
    }
}
