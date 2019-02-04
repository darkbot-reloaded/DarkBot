package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;

import static com.github.manolo8.darkbot.Main.API;

public class Clickable extends Updatable {

    private long confirm;

    public int radius;
    public int priority;

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
            API.writeMemoryInt(address + 40, radius);
        }
    }

    public void reset() {

        update();

        if (checkIntegrity()) {
            if (defRadius != 0 && defRadius != radius) API.writeMemoryInt(address + 40, defRadius);
            if (defPriority != 0 && defPriority != priority) API.writeMemoryInt(address + 44, defPriority);
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
        this.radius = API.readMemoryInt(address + 40);
        this.priority = API.readMemoryInt(address + 44);
    }

    @Override
    public void update(long address) {
        super.update(address);
        this.confirm = API.readMemoryLong(address);
    }
}
