package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.Clickable;
import com.github.manolo8.darkbot.core.objects.LocationInfo;
import com.github.manolo8.darkbot.core.objects.swf.Array;

import static com.github.manolo8.darkbot.Main.API;

public class Entity extends Updatable {

    public int id;

    public LocationInfo locationInfo;
    public Clickable clickable;

    public boolean removed;

    public Array traits;

    public Entity() {
        this.locationInfo = new LocationInfo(0);
        this.clickable = new Clickable();
        this.traits = new Array(0);
    }

    public Entity(int id) {
        this();
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public long getAddress() {
        return address;
    }

    public LocationInfo getLocationInfo() {
        return locationInfo;
    }

    public boolean isInvalid(long mapAddress) {

        int id = API.readMemoryInt(address + 56);
        long container = API.readMemoryLong(address + 96);

        return container != mapAddress || this.id != id;
    }

    @Override
    public void update() {
        locationInfo.update();
        clickable.update();
    }

    @Override
    public void update(long address) {
        super.update(address);

        this.locationInfo.update(API.readMemoryLong(address + 64));
        this.traits.update(API.readMemoryLong(address + 48));

        traits.update();

        for (int c = 0; c < traits.size; c++) {
            long adr = traits.elements[c];

            int radius = API.readMemoryInt(adr + 40);
            int priority = API.readMemoryInt(adr + 44);
            int enabled = API.readMemoryInt(adr + 48);

            if (radius >= 0 && radius < 4000 && priority > -4 && priority < 1000 && (enabled == 1 || enabled == 0)) {
                clickable.update(adr);
                break;
            }
        }
    }

    public void removed() {
        removed = true;
    }
}
