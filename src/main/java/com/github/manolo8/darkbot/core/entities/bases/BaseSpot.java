package com.github.manolo8.darkbot.core.entities.bases;

import com.github.manolo8.darkbot.core.entities.BasePoint;

/**
 * Generic entity for relevant spots of a base
 */
public abstract class BaseSpot extends BasePoint {
    public BaseSpot(int id, long address) {
        super(id, address);
    }
}
