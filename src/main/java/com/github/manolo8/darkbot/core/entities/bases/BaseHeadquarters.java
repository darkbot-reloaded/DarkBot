package com.github.manolo8.darkbot.core.entities.bases;

import eu.darkbot.api.entities.Station;

public class BaseHeadquarters extends BaseSpot implements Station.Headquarter {
    public BaseHeadquarters(int id, long address) {
        super(id, address);
    }
}
