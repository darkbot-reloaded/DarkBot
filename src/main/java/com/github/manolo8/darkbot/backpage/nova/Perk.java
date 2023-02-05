package com.github.manolo8.darkbot.backpage.nova;

import java.util.ArrayList;
import java.util.List;

public class Perk {
    private int perkId;
    private String perkTypeId;
    private int level;
    private int levelMax;
    private int drain;
    private double modifier;
    private String name;
    private String desc;
    private String drainDesc;
    private List<Upgrade> upgrades = new ArrayList<Upgrade>();

    public int getPerkId() {
        return perkId;
    }

    public void setPerkId(int perkId) {
        this.perkId = perkId;
    }

    public String getPerkTypeId() {
        return perkTypeId;
    }

    public void setPerkTypeId(String perkTypeId) {
        this.perkTypeId = perkTypeId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevelMax() {
        return levelMax;
    }

    public void setLevelMax(int levelMax) {
        this.levelMax = levelMax;
    }

    public int getDrain() {
        return drain;
    }

    public void setDrain(int drain) {
        this.drain = drain;
    }

    public double getModifier() {
        return modifier;
    }

    public void setModifier(double modifier) {
        this.modifier = modifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDrainDesc() {
        return drainDesc;
    }

    public void setDrainDesc(String drainDesc) {
        this.drainDesc = drainDesc;
    }

    public List<Upgrade> getUpgrades() {
        return upgrades;
    }

    public void setUpgrades(List<Upgrade> upgrades) {
        this.upgrades = upgrades;
    }

}
