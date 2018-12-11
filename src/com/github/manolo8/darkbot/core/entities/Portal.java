package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.objects.Location;
import com.github.manolo8.darkbot.core.objects.Map;

import static com.github.manolo8.darkbot.Main.API;

public class Portal extends Entity {

    public Map target;

    public int type;

    public Location iconLocation;

    public int targetId;

    public Portal(long address, int id, int targetId) {
        super(address, id);

        this.targetId = targetId;
        this.iconLocation = new Location(0, 0);
    }

    @Override
    public void update() {
        super.update();

        type = API.readMemoryInt(address + 112);

        iconLocation.x = location.x;
        iconLocation.y = location.y + 250;
    }
}
