package com.github.manolo8.darkbot.core.itf;

import com.github.manolo8.darkbot.core.utils.pathfinder.Area;

public interface Obstacle {

    Area getArea();

    boolean isRemoved();

    boolean use();
}
