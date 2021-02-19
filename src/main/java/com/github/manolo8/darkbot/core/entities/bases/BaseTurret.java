package com.github.manolo8.darkbot.core.entities.bases;

import com.github.manolo8.darkbot.core.entities.BasePoint;
import eu.darkbot.api.entities.Station;

/**
 * Small turrets around x-1 and x-8 bases
 */
public class BaseTurret extends BasePoint implements Station.Turret {
    public BaseTurret(int id, long address) {
        super(id, address);
    }
}
