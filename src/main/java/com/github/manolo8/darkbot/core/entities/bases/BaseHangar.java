package com.github.manolo8.darkbot.core.entities.bases;


import eu.darkbot.api.entities.Station;

public class BaseHangar extends BaseSpot implements Station.Hangar {
    public BaseHangar(int id, long address) {
        super(id, address);
    }
}
