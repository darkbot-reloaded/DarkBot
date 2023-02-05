package com.github.manolo8.darkbot.backpage.nova;

import java.util.ArrayList;
import java.util.List;

public class Agent {
    private int captainId;
    private String captainTypeId;
    private String name;
    private int energy;
    private int energyMax;
    private long energyFullTime;
    private List<Perk> perks = new ArrayList<Perk>();
    private Boolean equipped;
    private DismissCost dismissCost;
    private int regenAmount;
    private double regenRate;
    private String desc;
    private boolean forRemoval;

    public boolean getForRemoval() {
        return this.forRemoval;
    }

    public void setForRemoval(boolean forRemoval) {
        this.forRemoval = forRemoval;
    }

    public int getCaptainId() {
        return captainId;
    }

    public void setCaptainId(int captainId) {
        this.captainId = captainId;
    }

    public String getCaptainTypeId() {
        return captainTypeId;
    }

    public void setCaptainTypeId(String captainTypeId) {
        this.captainTypeId = captainTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getEnergyMax() {
        return energyMax;
    }

    public void setEnergyMax(int energyMax) {
        this.energyMax = energyMax;
    }

    public long getEnergyFullTime() {
        return energyFullTime;
    }

    public void setEnergyFullTime(long energyFullTime) {
        this.energyFullTime = energyFullTime;
    }

    public List<Perk> getPerks() {
        return perks;
    }

    public void setPerks(List<Perk> perks) {
        this.perks = perks;
    }

    public Boolean getEquipped() {
        return equipped;
    }

    public void setEquipped(Boolean equipped) {
        this.equipped = equipped;
    }

    public DismissCost getDismissCost() {
        return dismissCost;
    }

    public void setDismissCost(DismissCost dismissCost) {
        this.dismissCost = dismissCost;
    }

    public int getRegenAmount() {
        return regenAmount;
    }

    public void setRegenAmount(int regenAmount) {
        this.regenAmount = regenAmount;
    }

    public double getRegenRate() {
        return regenRate;
    }

    public void setRegenRate(double regenRate) {
        this.regenRate = regenRate;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


}
