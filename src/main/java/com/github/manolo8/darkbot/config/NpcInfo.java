package com.github.manolo8.darkbot.config;

import java.util.HashSet;

public class NpcInfo {

    public int priority;

    public HashSet<Integer> mapList = new HashSet<>();

    public boolean kill;
    public boolean killOnlyIfIsLast;

    public double radius;
    public boolean noCircle;
    public boolean ignoreAttacked;
    public Character attackKey;
}
