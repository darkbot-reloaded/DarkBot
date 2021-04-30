package com.github.manolo8.darkbot.core.objects;

import eu.darkbot.api.entities.utils.GameMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Map implements Comparable<Map>, GameMap {

    public int id;
    public String name, shortName;
    public boolean pvp;
    public boolean gg;

    public Map(int id, String name, boolean pvp, boolean gg) {
        this(id, name, name, pvp, gg);
    }

    public Map(int id, String name, String shortName, boolean pvp, boolean gg) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.pvp = pvp;
        this.gg = gg;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(@NotNull Map o) {
        return name.compareTo(o.name);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Nullable String getShortName() {
        return shortName;
    }

    @Override
    public boolean isPvp() {
        return pvp;
    }

    @Override
    public boolean isGG() {
        return gg;
    }

}
