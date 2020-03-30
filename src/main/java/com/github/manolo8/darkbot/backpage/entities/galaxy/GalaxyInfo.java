package com.github.manolo8.darkbot.backpage.entities.galaxy;

import com.github.manolo8.darkbot.utils.XmlHelper;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.stream.IntStream;

public class GalaxyInfo {
    private Integer money;
    private Integer samples;
    private Integer selectedSpinAmount;
    private Integer spinOnSale;
    private Integer spinSalePercentage;
    private Integer galaxyGateDay;
    private Integer bonusRewardsDay;
    private Integer energyCost;

    private List<Item> items = new ArrayList<>();
    private Map<GalaxyGate, Gate> gates = new HashMap<>();

    public void update(Element e) {
        this.items.clear();

        this.money              = XmlHelper.valueToInt(e, "money");
        this.samples            = XmlHelper.valueToInt(e, "samples");
        this.selectedSpinAmount = XmlHelper.valueToInt(e, "spinamount_selected");
        this.spinOnSale         = XmlHelper.valueToInt(e, "spinOnSale");
        this.spinSalePercentage = XmlHelper.valueToInt(e, "spinSalePercentage");
        this.galaxyGateDay      = XmlHelper.valueToInt(e, "galaxyGateDay");
        this.bonusRewardsDay    = XmlHelper.valueToInt(e, "bonusRewardsDay");
        this.energyCost         = XmlHelper.valueToInt(XmlHelper.getElement(e, "energy_cost"));

        if (XmlHelper.hasAnyElement(e, "gates"))       updateGates(XmlHelper.getElement(e, "gates"));
        if (XmlHelper.hasAnyElement(e, "items"))       updateItems(XmlHelper.getElement(e, "items"));
        if (XmlHelper.hasAnyElement(e, "multipliers")) updateMultipliers(XmlHelper.getElement(e, "multipliers"));

        if (XmlHelper.hasAnyElement(e, "setup")) {
            Integer id = XmlHelper.attrToInt(XmlHelper.getElement(e, "setup"), "gate_id");
            Optional.ofNullable(getGate(id)).ifPresent(Gate::onGatePrepare);
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

    public Integer getSpinSalePercentage() {
        return spinSalePercentage;
    }

    public Integer getEnergyCost() {
        return energyCost;
    }

    public boolean isSpinOnSale() {
        return spinOnSale != null && spinOnSale == 1;
    }

    public boolean isGalaxyGateDay() {
        return galaxyGateDay != null && galaxyGateDay == 1;
    }

    public boolean isBonusRewardsDay() {
        return bonusRewardsDay != null && bonusRewardsDay == 1;
    }

    public List<Item> getItems() {
        return items;
    }

    public Gate getGate(Integer id) {
        if (id == null) return null;
        return Arrays.stream(GalaxyGate.values())
                .filter(gate -> gate.getId() == id)
                .findFirst()
                .map(this::getGate).orElse(null);
    }

    public Gate getGate(GalaxyGate gate) {
        return getGates().get(gate);
    }

    public Map<GalaxyGate, Gate> getGates() {
        return gates;
    }

    private void updateItems(Element e) {
        NodeList list = e.getElementsByTagName("item");

        IntStream.range(0, list.getLength()).mapToObj(i -> (Element) list.item(i))
                .forEach(item -> {
                    Optional.ofNullable(getGate(XmlHelper.attrToInt(item, "gate_id"))).ifPresent(gate -> gate.update(item));
                    this.items.add(new Item().update(item));
                });
    }

    private void updateGates(Element e) {
        NodeList list = e.getElementsByTagName("gate");

        IntStream.range(0, list.getLength()).mapToObj(i -> (Element) list.item(i))
                .forEach(gate -> Arrays.stream(GalaxyGate.values())
                        .filter(g -> g.match(XmlHelper.attrToInt(gate, "id")))
                        .findFirst()
                        .ifPresent(g -> this.gates.computeIfAbsent(g, k -> new Gate()).update(gate)));
    }

    private void updateMultipliers(Element e) {
        NodeList list = e.getElementsByTagName("multiplier");

        IntStream.range(0, list.getLength()).mapToObj(i -> (Element) list.item(i))
                .forEach(multiplier -> Arrays.stream(GalaxyGate.values())
                        .filter(g -> g.match(multiplier.getAttribute("mode")))
                        .findFirst()
                        .ifPresent(g -> this.gates.computeIfAbsent(g, k -> new Gate()).setMultiplier(multiplier)));
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
                ", energyCost=" + energyCost +
                ", items=" + items +
                ", gates=" + gates +
                '}';
    }
}
