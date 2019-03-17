package com.github.manolo8.darkbot.core.objects;

public class Map {

    public int id;
    public String name;
    public boolean pvp;
    public boolean gg;

    public Map(int id, String name, boolean pvp, boolean gg) {
        this.id = id;
        this.name = name;
        this.pvp = pvp;
        this.gg = gg;
    }

    @Override
    public String toString() {
        return name + "#" + id;
    }
}
