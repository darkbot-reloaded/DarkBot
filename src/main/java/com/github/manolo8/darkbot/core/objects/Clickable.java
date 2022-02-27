package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.itf.Updatable;

import static com.github.manolo8.darkbot.Main.API;

public class Clickable extends Updatable {

    public int radius;
    public int priority;
    public boolean enabled;

    public int defRadius = -1;
    public int defPriority = -1;

    private final Entity owner;

    public Clickable(Entity owner) {
        this.owner = owner;
    }

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
        return address == 0 || defRadius <= 0 || API.readLong(address + 32) != owner.address; // confirm owner address
    }

    @Override
    public void update() {
        if (address == 0) return;
        int oldRad = radius, oldPri = priority;
        this.radius = API.readMemoryInt(address + 40);
        this.priority = API.readMemoryInt(address + 44);
        this.enabled = API.readMemoryBoolean(address, 64, 32);

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
        this.radius = defRadius = API.readMemoryInt(address + 40);
        this.priority = defPriority = API.readMemoryInt(address + 44);
    }
}
