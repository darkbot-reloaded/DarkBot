package com.github.manolo8.darkbot.backpage.entities.galaxy;

import com.github.manolo8.darkbot.utils.XmlHelper;
import org.dom4j.Element;

import java.util.List;
import java.util.stream.Collectors;

public class GalaxyInfo {
    private Integer money;
    private Integer samples;
    private Integer spinOnSale;
    private Integer spinSalePercentage;
    private Integer galaxyGateDay;
    private Integer bonusRewardsDay;
    private EnergyCost energyCosts;

    private List<Multiplier> multipliers;
    private List<Gate> gates;
    private List<Item> items;

    public GalaxyInfo() {

    }

    public void updateGalaxyInfo(Element e) {
        this.money = XmlHelper.getValueInt(e, "money");
        this.samples = XmlHelper.getValueInt(e, "samples");
        this.spinOnSale = XmlHelper.getValueInt(e, "spinOnSale");
        this.spinSalePercentage = XmlHelper.getValueInt(e, "spinSalePercentage");
        this.galaxyGateDay = XmlHelper.getValueInt(e, "galaxyGateDay");
        this.bonusRewardsDay = XmlHelper.getValueInt(e, "bonusRewardsDay");
        this.energyCosts = new EnergyCost(e.element("energy_cost"));

        if (XmlHelper.hasChild(e, "multipliers"))
            this.multipliers = XmlHelper.childrenOf(e, "multipliers").map(Multiplier::new).collect(Collectors.toList());
        if (XmlHelper.hasChild(e, "gates"))
            this.gates = XmlHelper.childrenOf(e, "gates").map(Gate::new).collect(Collectors.toList());
        if (XmlHelper.hasChild(e, "items"))
            this.items = XmlHelper.childrenOf(e, "items").map(Item::new).collect(Collectors.toList());
    }

    public Integer getMoney() {
        return money;
    }

    public Integer getSamples() {
        return samples;
    }

    public Integer getSpinOnSale() {
        return spinOnSale;
    }

    public Integer getSpinSalePercentage() {
        return spinSalePercentage;
    }

    public Integer getGalaxyGateDay() {
        return galaxyGateDay;
    }

    public Integer getBonusRewardsDay() {
        return bonusRewardsDay;
    }

    public EnergyCost getEnergyCosts() {
        return energyCosts;
    }

    public List<Multiplier> getMultipliers() {
        return multipliers;
    }

    public List<Gate> getGates() {
        return gates;
    }

    public List<Item> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "GalaxyInfo{" +
                "money=" + money +
                ", samples=" + samples +
                ", spinOnSale=" + spinOnSale +
                ", spinSalePercentage=" + spinSalePercentage +
                ", galaxyGateDay=" + galaxyGateDay +
                ", bonusRewardsDay=" + bonusRewardsDay +
                '}';
    }
}
