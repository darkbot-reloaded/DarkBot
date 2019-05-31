package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.objects.Map;

public class Portal extends Entity {

    private PortalMatcher matcher;

    public Map target;
    public int type;

    public Portal(int searchId, int searchType, int searchX, int searchY, Map target) {
        super(-1);
        this.matcher = new PortalMatcher(searchId, searchType, searchX, searchY);

        super.removed = true;
        this.target = target;
    }

    public Portal(int id, int type, int x, int y) {
        this(id, type, x, y, null);
        this.id = id;
    }

    public boolean matches(long id, int x, int y, int type) {
        return matcher.matches(id, x, y, type);
    }

    @Override
    public void update() {
        super.update();

        type = Main.API.readMemoryInt(address + 112);
        if (locationInfo.isMoving()) ConfigEntity.INSTANCE.updateSafetyFor(this);
    }

    @Override
    public void added() {
        super.added();
        ConfigEntity.INSTANCE.updateSafetyFor(this);
    }

    @Override
    public String toString() {
        return "(" + locationInfo.now + ")" + type;
    }

    // Holds the search criteria portals in the star manager
    private class PortalMatcher {
        private long searchId;
        private int searchType, searchX, searchY;

        PortalMatcher(int searchId, int searchType, int searchX, int searchY) {
            this.searchId = searchId;
            this.searchType = searchType;
            this.searchX = searchX;
            this.searchY = searchY;
        }

        boolean matches(long id, int x, int y, int type) {
            return (searchId != -1 && searchId == id) || // By id
                    (searchType != 1 && searchType == type) || // By type
                    (searchX != -1 && searchY != -1 && searchX == x && searchY == y); // By pos
        }

    }

}
