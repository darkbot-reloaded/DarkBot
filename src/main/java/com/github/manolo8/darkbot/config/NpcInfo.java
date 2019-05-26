package com.github.manolo8.darkbot.config;

import java.util.HashSet;
import java.util.Set;

public class NpcInfo {

    public int priority;

    public Set<Integer> mapList = new HashSet<>();

    public boolean kill;

    public double radius;
    public boolean noCircle;
    public boolean ignoreOwnership;
    public boolean ignoreAttacked;
    public boolean attackSecond;
    public boolean passive;
    public Character attackKey;

    public void copyOf(NpcInfo other) {
        this.priority = other.priority;
        this.kill = other.kill;
        this.radius = other.radius;
        this.noCircle = other.noCircle;
        this.ignoreOwnership = other.ignoreOwnership;
        this.ignoreAttacked = other.ignoreAttacked;
        this.attackSecond = other.attackSecond;
        this.passive = other.passive;
        this.attackKey = other.attackKey;
    }
}
