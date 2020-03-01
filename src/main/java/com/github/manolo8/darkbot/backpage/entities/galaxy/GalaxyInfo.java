package com.github.manolo8.darkbot.backpage.entities.galaxy;

import com.github.manolo8.darkbot.utils.XmlHelper;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class GalaxyInfo {
    private Integer money;
    private Integer samples;
    private Integer selectedSpinAmount;
    private Integer spinOnSale;
    private Integer spinSalePercentage;
    private Integer galaxyGateDay;
    private Integer bonusRewardsDay;
    private EnergyCost energyCost = new EnergyCost();

    private List<Multiplier> multipliers = new ArrayList<>();
    private List<Gate> gates = new ArrayList<>();
    private List<Item> items = new ArrayList<>();

    public void update(Element e) {
        this.money = XmlHelper.valueToInt(e, "money");
        this.samples = XmlHelper.valueToInt(e, "samples");
        this.selectedSpinAmount = XmlHelper.valueToInt(e, "spinamount_selected");
        this.spinOnSale = XmlHelper.valueToInt(e, "spinOnSale");
        this.spinSalePercentage = XmlHelper.valueToInt(e, "spinSalePercentage");
        this.galaxyGateDay = XmlHelper.valueToInt(e, "galaxyGateDay");
        this.bonusRewardsDay = XmlHelper.valueToInt(e, "bonusRewardsDay");
        this.energyCost.update(XmlHelper.getElement(e, "energy_cost"));

        if (XmlHelper.hasAnyElement(e, "multipliers")) updateMultipliersList(XmlHelper.getElement(e, "multipliers"));
        if (XmlHelper.hasAnyElement(e, "gates")) updateGatesList(XmlHelper.getElement(e, "gates"));
        if (XmlHelper.hasAnyElement(e, "items")) updateItemsList(XmlHelper.getElement(e, "items"));
        else this.items.clear();
    }

    private void updateItemsList(Element e) {
        NodeList list = e.getElementsByTagName("item");
        this.items.clear();

        for (int i = 0; i < list.getLength(); i++) {
            e = (Element) list.item(i);
            this.items.add(new Item().update(e));
        }
    }

    private void updateGatesList(Element e) {
        NodeList list = e.getElementsByTagName("gate");

        for (int i = 0; i < list.getLength(); i++) {
            e = (Element) list.item(i);
            Integer id = XmlHelper.attrToInt(e, "id");

            int index = IntStream.range(0, gates.size())
                    .filter(idx -> gates.get(idx).alreadyInList(id))
                    .findFirst()
                    .orElse(-1);

            if (index == -1) gates.add(new Gate().update(e));
            else gates.set(index, gates.get(index).update(e));
        }
    }

    private void updateMultipliersList(Element e) {
        NodeList list = e.getElementsByTagName("multiplier");

        for (int i = 0; i < list.getLength(); i++) {
            e = (Element) list.item(i);
            String mode = e.getAttribute("mode");

            int index = IntStream.range(0, multipliers.size())
                    .filter(idx -> multipliers.get(idx).alreadyInList(mode))
                    .findFirst()
                    .orElse(-1);

            if (index == -1) multipliers.add(new Multiplier().update(e));
            else multipliers.set(index, multipliers.get(index).update(e));
        }
    }

    public Integer getMoney() {
        return money;
    }

    public Integer getSamples() {
        return samples;
    }

    public Integer getSelectedSpinAmount() {
        return selectedSpinAmount;
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

    public EnergyCost getEnergyCost() {
        return energyCost;
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
                ", selectedSpinAmount=" + selectedSpinAmount +
                ", spinOnSale=" + spinOnSale +
                ", spinSalePercentage=" + spinSalePercentage +
                ", galaxyGateDay=" + galaxyGateDay +
                ", bonusRewardsDay=" + bonusRewardsDay +
                ", energyCosts=" + energyCost +
                ", multipliers=" + multipliers +
                ", gates=" + gates +
                ", items=" + items +
                '}';
    }
}
