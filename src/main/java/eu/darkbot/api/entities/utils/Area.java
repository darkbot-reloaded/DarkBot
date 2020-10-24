package eu.darkbot.api.entities.utils;

import java.awt.Shape;

public interface Area extends Shape {//?

    AreaType getAreaType();

    enum AreaType {
        RECTANGLE,
        CIRCULAR,
        TRIANGLE,
        UNKNOWN
    }
}