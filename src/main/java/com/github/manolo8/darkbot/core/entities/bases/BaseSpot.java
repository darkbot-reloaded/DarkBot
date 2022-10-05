package com.github.manolo8.darkbot.core.entities.bases;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.entities.BasePoint;

/**
 * Generic entity for relevant spots of a base
 */
public abstract class BaseSpot extends BasePoint {
    public BaseSpot(int id, long address) {
        super(id, address);
    }

    @Override
    public void update() {
        super.update();
        clickable.update();

        if (locationInfo.isMoving()) ConfigEntity.INSTANCE.updateSafetyFor(this);
    }

    @Override
    public void removed() {
        super.removed();
        ConfigEntity.INSTANCE.updateSafetyFor(this);
    }

}
