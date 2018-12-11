package com.github.manolo8.darkbot.core.objects;

public class NpcType {

    public String name;

    public int radius;
    public boolean kamikazeOnGG;

    public NpcType(String name, int radius) {
        this.name = name;
        this.radius = radius;
    }

    public NpcType(String name, int radius, boolean kamikazeOnGG) {
        this.name = name;
        this.radius = radius;
        this.kamikazeOnGG = kamikazeOnGG;
    }
}
