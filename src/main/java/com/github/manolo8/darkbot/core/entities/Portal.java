package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.objects.Map;

import static com.github.manolo8.darkbot.Main.API;

public class Portal extends Entity {

    public Map target;
    public int type;
    public int x, y;

    public Portal(int id, int type, int x, int y, Map target) {
        super(id);
        this.type = type;
        this.x = x;
        this.y = y;
        this.target = target;
    }

    public boolean inLoc(int x, int y) {
        return this.x == x && this.y == y;
    }

    @Override
    public void update() {
        super.update();

        //type = API.readMemoryInt(address + 112);
    }
}
