package com.github.manolo8.darkbot.core.entities.bases;


import eu.darkbot.api.entities.Station;

public class BaseRepairStation extends BaseSpot implements Station.Repair {
    public BaseRepairStation(int id, long address) {
        super(id, address);
    }
}
