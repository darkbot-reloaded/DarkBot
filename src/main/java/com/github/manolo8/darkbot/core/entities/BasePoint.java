package com.github.manolo8.darkbot.core.entities;

import eu.darkbot.api.entities.Station;

public class BasePoint extends Entity implements Station {

    public BasePoint(int id, long address) {
        super(id, address);
    }
}
