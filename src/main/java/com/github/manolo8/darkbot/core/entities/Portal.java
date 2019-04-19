package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.objects.Map;

public class Portal extends Entity {

    public Map target;
    public int type;
    public int searchType;
    public int x, y;

    public Portal(int id, int searchType, int x, int y, Map target) {
        super(id);
        super.removed = true;
        this.searchType = searchType;
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

        type = Main.API.readMemoryInt(address + 112);
    }

    @Override
    public String toString() {
        return id + "," + type;
    }
}
