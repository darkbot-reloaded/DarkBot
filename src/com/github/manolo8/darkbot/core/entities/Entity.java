package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.def.Updatable;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.objects.Location;

import static com.github.manolo8.darkbot.Main.API;

public class Entity implements Updatable {

    public long address;
    public int id;
    public Location location;

    public Entity() {
        this.location = new Location(0);
    }

    public Entity(long address, int id) {
        this.address = address;
        this.id = id;
        this.location = new Location(API.readMemoryLong(address + 64));
    }

    public int getId() {
        return id;
    }

    public long getAddress() {
        return address;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isInvalid() {

        int id = API.readMemoryInt(address + 56);
        long container = API.readMemoryLong(address + 96);

        return container != MapManager.mapAddress || this.id != id;
    }

    @Override
    public void update() {
        location.update();
    }

    @Override
    public void update(long address) {
        this.address = address;
        this.location.update(API.readMemoryLong(address + 64));
    }
}
