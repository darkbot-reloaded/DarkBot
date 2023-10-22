package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.itf.Obstacle;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.utils.pathfinder.AreaImpl;
import com.github.manolo8.darkbot.core.utils.pathfinder.CircleImpl;
import eu.darkbot.api.game.other.Location;
import eu.darkbot.util.Timer;

import static com.github.manolo8.darkbot.Main.API;

public class Mine extends Entity implements Obstacle, eu.darkbot.api.game.entities.Mine {
    // A bit of a bigger avoid area on these
    private static final int FROZEN_LAB_MINE = 21;

    public int typeId;

    private final CircleImpl area = new CircleImpl(0, 0, 200);

    private Mine(int id) {
        super(id);
    }

    public Mine(int id, long address) {
        super(id);
        this.update(address);
    }

    @Override
    public void update(long address) {
        super.update(address);

        this.typeId = API.readMemoryInt(address + 112);
    }

    @Override
    public void update() {
        super.update();

        if (locationInfo.isMoving())
            area.set(locationInfo.now, typeId == FROZEN_LAB_MINE ? 500 : 200);
    }

    @Override
    public AreaImpl getArea() {
        return area;
    }

    @Override
    public boolean isRemoved() {
        return removed;
    }

    @Override
    public boolean use() {
        return main.config.MISCELLANEOUS.AVOID_MINES;
    }

    @Override
    public String toString() {
        return typeId + "";
    }

    @Override
    public int getTypeId() {
        return typeId;
    }

    public static class FakeMine extends Mine implements eu.darkbot.api.game.entities.FakeEntity.FakeMine {
        private static int CURR_ID = Integer.MIN_VALUE;
        private Timer timeout;
        private long removeDistance;

        public FakeMine(int typeId, Location loc, long removeDistance, long keepAlive) {
            super(CURR_ID++);
            super.locationInfo.updatePosition(loc.x(), loc.y());
            super.main = HeroManager.instance.main;
            super.typeId = typeId;
            super.area.set(locationInfo.now, typeId == FROZEN_LAB_MINE ? 500 : 200);
            super.removed = false;
            setTimeout(keepAlive);
            setRemoveDistance(removeDistance);
        }

        public void setTimeout(long keepAlive) {
            if (keepAlive != -1) {
                timeout = Timer.get(keepAlive);
                timeout.activate();
            }
            else timeout = null;
        }

        public void setRemoveDistance(long removeDistance) {
            this.removeDistance = removeDistance;
        }

        public boolean isInvalid(long mapAddress) {
            if (timeout != null && timeout.isInactive()) return true;
            return removeDistance == -1 || HeroManager.instance.distanceTo(this) < removeDistance;
        }

        public void update() {}

        public void update(long address) {}
    }
}
