package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.objects.Map;
import eu.darkbot.api.objects.EntityInfo;

import java.util.Optional;

import static com.github.manolo8.darkbot.Main.API;

public class Portal extends Entity implements eu.darkbot.api.entities.Portal {
    public static final int TYPE_OFFSET = 120;

    public final Map target;
    public final int factionId;
    private final PortalMatcher matcher;
    public boolean isJumping;
    public int type;

    public Portal(int id, int type, int x, int y) {
        this(id, type, x, y, null, -1);
    }

    public Portal(int searchType, int searchX, int searchY, Map target, int factionId) {
        this(-1, searchType, searchX, searchY, target, factionId);
    }

    public Portal(int id, int searchType, int searchX, int searchY, Map target, int factionId) {
        super(id);
        this.matcher = new PortalMatcher(searchType, searchX, searchY, target != null && target.id == 71);

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

        isJumping = API.readMemoryBoolean(address + 112) || API.readMemoryBoolean(address + 116);
        type = API.readMemoryInt(address + TYPE_OFFSET);
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
        // Avoid matching if portal is centered. Used to avoid zeta being in the wrong place
        private final boolean noCenter;

        PortalMatcher(int searchType, int searchX, int searchY, boolean noCenter) {
            this.searchType = searchType;
            this.searchX    = searchX;
            this.searchY    = searchY;
            this.noCenter   = noCenter;
        }

        boolean matches(int x, int y, int type) {
            return (searchType == -1 || searchType == type) && // By type
                    (searchX == -1 || searchX == x) && (searchY == -1 || searchY == y) && // By pos
                    (!noCenter || x != 10500 && y != 6500); // Avoid centered
        }
    }

    @Override
    public Optional<eu.darkbot.api.entities.utils.Map> getTargetMap() {
        return Optional.ofNullable(target);
    }

    @Override
    public int getTypeId() {
        return type;
    }

    @Override
    public EntityInfo.Faction getFaction() {
        return EntityInfo.Faction.of(factionId);
    }
}
