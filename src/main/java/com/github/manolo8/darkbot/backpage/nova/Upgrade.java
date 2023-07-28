package com.github.manolo8.darkbot.backpage.nova;

import java.util.ArrayList;
import java.util.List;

public class Upgrade {
    private int level;
    private double modifier;
    private List<Cost> cost = new ArrayList<Cost>();

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getModifier() {
        return modifier;
    }

    public void setModifier(double modifier) {
        this.modifier = modifier;
    }

    public List<Cost> getCost() {
        return cost;
    }

    public void setCost(List<Cost> cost) {
        this.cost = cost;
    }
}
