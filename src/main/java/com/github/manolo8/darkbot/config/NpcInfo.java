package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.types.Option;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Option(value = "Name", description = "Npc names, npcs ending in * are grouped")
public class NpcInfo {

    @Option(value = "Radius", description = "Distance to keep from this npc")
    public double radius;
    @Option(value = "Priority", description = "#1 priority will be targeted before #2 priority")
    public int priority;
    @Option(value = "Kill", description = "If this npc should be killed")
    public boolean kill;
    @Option(value = "Ammo key", description = "Special ammo for this npc, if unset, default is used")
    public Character attackKey;

    @Option(value = "Extra", description = "Several extra flags for the npc")
    public ExtraNpcInfo extra = new ExtraNpcInfo();

    public static class ExtraNpcInfo {
        public boolean noCircle;
        public boolean ignoreOwnership;
        public boolean ignoreAttacked;
        public boolean attackSecond;
        public boolean passive;

        @Override
        public String toString() {
            return Stream.of(
                    noCircle ? "NC" : null,
                    ignoreOwnership ? "IO" : null,
                    ignoreAttacked ? "IA" : null,
                    passive ? "P" : null,
                    attackSecond ? "AS" : null
            ).filter(Objects::nonNull).collect(Collectors.joining(","));
        }
    }

    public Set<Integer> mapList = new HashSet<>();

    public void copyOf(NpcInfo other) {
        this.radius = other.radius;
        this.priority = other.priority;
        this.kill = other.kill;
        this.attackKey = other.attackKey;

        this.extra.noCircle = other.extra.noCircle;
        this.extra.ignoreOwnership = other.extra.ignoreOwnership;
        this.extra.ignoreAttacked = other.extra.ignoreAttacked;
        this.extra.attackSecond = other.extra.attackSecond;
        this.extra.passive = other.extra.passive;
    }
}
