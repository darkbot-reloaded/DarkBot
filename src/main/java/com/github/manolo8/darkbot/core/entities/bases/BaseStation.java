package com.github.manolo8.darkbot.core.entities.bases;


import eu.darkbot.api.entities.Station;

public class BaseStation extends BaseSpot implements Station.HomeBase {
    public BaseStation(int id, long address) {
        super(id, address);
    }
}
