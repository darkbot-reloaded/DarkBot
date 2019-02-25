package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;

import static com.github.manolo8.darkbot.Main.API;

public class Clickable extends Updatable {

    private long confirm;

    public int radius = -1;
    public int priority = -1;

    private int defRadius;
    private int defPriority;

    public void setPriority(int priority) {

        update();

        if (this.priority != priority && checkIntegrity()) {
            if (defPriority == 0) defPriority = this.priority;
            API.writeMemoryInt(address + 44, this.priority = priority);
        }
    }

    public void setRadius(int radius) {

        update();

        if (this.radius != radius && checkIntegrity()) {
            if (defRadius == 0) defRadius = this.radius;
            API.writeMemoryInt(address + 40, this.radius = radius);
        }
    }

    public void reset() {

        update();

        if (checkIntegrity()) {
            if (defRadius != 0 && defRadius != radius) API.writeMemoryInt(address + 40, radius = defRadius);
            if (defPriority != 0 && defPriority != priority) API.writeMemoryInt(address + 44, priority = defPriority);
            defRadius = 0;
            defPriority = 0;
        }
    }

    /**
     * @return prevent swf crash
     */
    private boolean checkIntegrity() {
        return API.readMemoryLong(address) == confirm;
    }

    @Override
    public void update() {
        int oldRad = radius, oldPri = priority;
        this.radius = API.readMemoryInt(address + 40);
        this.priority = API.readMemoryInt(address + 44);

        if (oldRad != -1 && oldRad != radius) setRadius(oldRad);
        if (oldPri != -1 && oldPri != priority) setPriority(oldPri);
    }

    @Override
    public void update(long address) {
        super.update(address);
        this.confirm = API.readMemoryLong(address);
    }
}
