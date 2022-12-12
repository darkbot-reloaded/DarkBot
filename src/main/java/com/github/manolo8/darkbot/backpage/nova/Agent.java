package com.github.manolo8.darkbot.backpage.nova;

import java.util.ArrayList;
import java.util.List;

public class Agent {
    private String captainId;
    private String captainTypeId;
    private String name;
    private String energy;
    private String energyMax;
    private String energyFullTime;
    private List<Perk> perks = new ArrayList<Perk>();
    private Boolean equipped;
    private DismissCost dismissCost;
    private String regenAmount;
    private String regenRate;
    private String desc;
    private boolean forRemoval;

    public boolean getForRemoval() {
        return this.forRemoval;
    }

    public void setForRemoval(boolean forRemoval) {
        this.forRemoval = forRemoval;
    }

    public String getCaptainId() {
        return captainId;
    }

    public void setCaptainId(String captainId) {
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

    public String getEnergy() {
        return energy;
    }

    public void setEnergy(String energy) {
        this.energy = energy;
    }

    public String getEnergyMax() {
        return energyMax;
    }

    public void setEnergyMax(String energyMax) {
        this.energyMax = energyMax;
    }

    public String getEnergyFullTime() {
        return energyFullTime;
    }

    public void setEnergyFullTime(String energyFullTime) {
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

    public String getRegenAmount() {
        return regenAmount;
    }

    public void setRegenAmount(String regenAmount) {
        this.regenAmount = regenAmount;
    }

    public String getRegenRate() {
        return regenRate;
    }

    public void setRegenRate(String regenRate) {
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
        private String amount;

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }
    }

    public class Perk {
        private String perkId;
        private String perkTypeId;
        private Integer level;
        private Integer levelMax;
        private String drain;
        private double modifier;
        private String name;
        private String desc;
        private String drainDesc;
        private List<Upgrade> upgrades = new ArrayList<Upgrade>();

        public String getPerkId() {
            return perkId;
        }

        public void setPerkId(String perkId) {
            this.perkId = perkId;
        }

        public String getPerkTypeId() {
            return perkTypeId;
        }

        public void setPerkTypeId(String perkTypeId) {
            this.perkTypeId = perkTypeId;
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }

        public Integer getLevelMax() {
            return levelMax;
        }

        public void setLevelMax(Integer levelMax) {
            this.levelMax = levelMax;
        }

        public String getDrain() {
            return drain;
        }

        public void setDrain(String drain) {
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
        private Integer amount;
        private String item;

        public Integer getAmount() {
            return amount;
        }

        public void setAmount(Integer amount) {
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
        private Integer level;
        private double modifier;
        private List<Cost> cost = new ArrayList<Cost>();

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
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
