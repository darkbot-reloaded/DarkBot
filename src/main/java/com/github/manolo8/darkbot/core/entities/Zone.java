package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.pathfinder.PolygonImpl;
import eu.darkbot.api.game.other.Area;
import eu.darkbot.api.game.other.Locatable;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class Zone extends Entity implements eu.darkbot.api.game.entities.Zone {

    private final ObjArray pointsArr = ObjArray.ofVector(true);
    private final List<Position> points = new ArrayList<>();
    private final PolygonImpl zoneArea = new PolygonImpl(points);

    Zone(int id, long address) {
        super(id);
        this.update(address);
    }

    @Override
    public void update() {
        super.update();

        pointsArr.update(API.readMemoryLong(address + 216));
        if (pointsArr.getSize() < 3) return;

        pointsArr.sync(points, Position::new, null);
        zoneArea.invalidateBounds();
    }

    @Override
    public Area getZoneArea() {
        return zoneArea;
    }

    private static class Position extends UpdatableAuto implements Locatable {

        private double x, y;

        @Override
        public double getX() {
            return x;
        }

        @Override
        public double getY() {
            return y;
        }

        @Override
        public void update() {
            if (address == 0) return;
            this.x = API.readMemoryDouble(address + 32);
            this.y = API.readMemoryDouble(address + 40);
        }
    }
}
