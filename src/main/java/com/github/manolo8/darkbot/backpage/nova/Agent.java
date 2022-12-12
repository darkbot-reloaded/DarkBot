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

    public class DismissCost {
        private String item;
        private int amount;

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }

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

    public class Cost {
        private int amount;
        private String item;

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }
    }

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
}
