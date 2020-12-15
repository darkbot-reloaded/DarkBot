package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.objects.Map;

public class Portal extends Entity {

    public final Map target;
    public final int factionId;
    private final PortalMatcher matcher;
    public int type;

    public Portal(int id, int type, int x, int y) {
        this(id, type, x, y, null, -1);
    }

    public Portal(int searchType, int searchX, int searchY, Map target, int factionId) {
        this(-1, searchType, searchX, searchY, target, factionId);
    }

    public Portal(int id, int searchType, int searchX, int searchY, Map target, int factionId) {
        super(id);
        this.matcher = new PortalMatcher(searchType, searchX, searchY);

        super.removed  = true;
        this.target    = target;
        this.factionId = factionId;
    }

    public boolean matches(int x, int y, int type) {
        return matcher.matches(x, y, type);
    }

    @Override
    public void update() {
        super.update();
        clickable.update();

        type = Main.API.readMemoryInt(address + 112);
        if (locationInfo.isMoving()) {
            ConfigEntity.INSTANCE.updateSafetyFor(this);
        }
    }

    @Override
    public void added(Main main) {
        super.added(main);
        ConfigEntity.INSTANCE.updateSafetyFor(this);
    }

    @Override
    public String toString() {
        if (!removed) return id + "(" + locationInfo.now + ")" + type;
        else return "(" + matcher.searchX + "," + matcher.searchY + ")" + matcher.searchType;
    }

    // Holds the search criteria portals in the star manager
    private static class PortalMatcher {
        private final int searchType, searchX, searchY;

        PortalMatcher(int searchType, int searchX, int searchY) {
            this.searchType = searchType;
            this.searchX    = searchX;
            this.searchY    = searchY;
        }

        boolean matches(int x, int y, int type) {
            return (searchType != -1 && searchType == type) || // By type
                    (searchX != -1 && searchY != -1 && searchX == x && searchY == y); // By pos
        }
    }
}
