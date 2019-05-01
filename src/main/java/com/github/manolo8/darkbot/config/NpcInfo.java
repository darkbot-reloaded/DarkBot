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
    public boolean passive;
    public Character attackKey;
}
