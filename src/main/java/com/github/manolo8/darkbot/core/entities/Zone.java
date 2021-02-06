package com.github.manolo8.darkbot.core.entities;

import com.github.manolo8.darkbot.core.objects.Point;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.pathfinder.RectangleImpl;
import eu.darkbot.api.entities.utils.Area;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class Zone
        extends Entity implements eu.darkbot.api.entities.Zone {

    private final RectangleImpl area = new RectangleImpl(0, 0, 0, 0);
    private final ObjArray pointsArr = ObjArray.ofVector(true);
    private final List<Point> points = new ArrayList<>();

    Zone(int id, long address) {
        super(id);
        this.update(address);
    }

    @Override
    public void update() {
        super.update();

        pointsArr.update(API.readMemoryLong(address + 216));
        if (pointsArr.getSize() < 3) return;
        pointsArr.sync(points, Point::new, null);

        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE, minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        for (Point p : points) {
            minX = Double.min(minX, p.x);
            maxX = Double.max(maxX, p.x);
            minY = Double.min(minY, p.y);
            maxY = Double.max(maxY, p.y);
        }

        area.set(minX, minY, maxX, maxY);
    }

    public RectangleImpl getZone() {
        return area;
    }

    @Override
    public Area getArea() {
        return null;
    }

    @Override
    public boolean isZoneValid() {
        return false;
    }
}
