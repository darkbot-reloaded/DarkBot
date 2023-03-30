package com.github.manolo8.darkbot.core.entities.bases;

import com.github.manolo8.darkbot.Main;
import eu.darkbot.api.game.entities.Station;

public class BaseRepairStation extends BaseSpot implements Station.Repair {
    public BaseRepairStation(int id, long address) {
        super(id, address);
    }

    public int getInstantRepairs() {
        try {
            return Integer.parseInt(Main.API.readString(clickable.address, 136, 40));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
