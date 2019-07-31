package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.gui.tree.components.JNpcInfoTable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Option("Name")
public class NpcInfo {

    @Option("Radius")
    public double radius;
    @Option("Priority")
    public int priority;
    @Option("Kill")
    public boolean kill;
    @Option("Ammo key")
    public Character attackKey;

    @Option("Extra")
    //@Editor()
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
