package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.entities.fake.FakeEntities;
import com.github.manolo8.darkbot.core.entities.fake.FakeExtension;
import com.github.manolo8.darkbot.core.itf.Obstacle;
import com.github.manolo8.darkbot.core.utils.pathfinder.AreaImpl;
import com.github.manolo8.darkbot.core.utils.pathfinder.CircleImpl;
import eu.darkbot.api.game.entities.FakeEntity;
import lombok.Getter;

import static com.github.manolo8.darkbot.Main.API;

public class Mine extends Entity implements Obstacle, eu.darkbot.api.game.entities.Mine {
    // A bit of a bigger avoid area on these
    private static final int FROZEN_LAB_MINE = 21;

    public int typeId;

    protected final CircleImpl area = new CircleImpl(0, 0, 200);

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

        this.typeId = API.readInt(address + 112);
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

    @Getter
    public static class Fake extends Mine implements FakeEntity.FakeMine, FakeExtension {
        private final FakeExtension.Data fakeData = new FakeExtension.Data(this);

        public Fake(int typeId) {
            super(FakeEntities.allocateFakeId());
            super.typeId = typeId;
        }

        @Override
        public boolean isInvalid(long mapAddress) {
            return fakeData.isInvalid();
        }

        @Override
        public boolean trySelect(boolean tryAttack) {
            return fakeData.trySelect(tryAttack);
        }

        @Override
        public void update() {
            if (locationInfo.isMoving())
                area.set(locationInfo.now, typeId == FROZEN_LAB_MINE ? 500 : 200);
        }

        @Override
        public void update(long address) {
        }
    }
}
