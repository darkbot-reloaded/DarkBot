package com.github.manolo8.darkbot.backpage.hangar;

import java.util.List;

public class General {
    private Ship ship;
    private Pet pet;
    private List<Drone> drones;

    public Ship getShip() {
        return ship;
    }

    public Pet getPet() {
        return pet;
    }

    public List<Drone> getDrones() {
        return drones;
    }

    @Override
    public String toString() {
        return "General{" +
                "ship=" + ship +
                ", pet=" + pet +
                ", drones=" + drones +
                '}';
    }
}
