package com.github.manolo8.darkbot.backpage.entities.galaxy;

import com.github.manolo8.darkbot.utils.XmlHelper;
import org.dom4j.Element;

public class Item {
    private String date;
    private String state;
    private String type;

    private Integer gateId;
    private Integer duplicate;
    private Integer partId;
    private Integer itemId;
    private Integer amount;
    private Integer current;
    private Integer total;
    private Integer multiplierUsed;

    public Item(String date, String state, String type, Integer gateId, Integer duplicate, Integer partId, Integer itemId, Integer amount, Integer current, Integer total, Integer multiplierUsed) {
        this.date = date;
        this.state = state;
        this.type = type;
        this.gateId = gateId;
        this.duplicate = duplicate;
        this.partId = partId;
        this.itemId = itemId;
        this.amount = amount;
        this.current = current;
        this.total = total;
        this.multiplierUsed = multiplierUsed;
    }

    public Item(Element e) {
        this(e.attributeValue("date"), e.attributeValue("state"), e.attributeValue("type"),
                XmlHelper.getAttrInt(e, "gate_id"), XmlHelper.getAttrInt(e, "duplicate"),
                XmlHelper.getAttrInt(e, "part_id"), XmlHelper.getAttrInt(e, "item_id"),
                XmlHelper.getAttrInt(e, "amount"), XmlHelper.getAttrInt(e, "current"),
                XmlHelper.getAttrInt(e, "total"), XmlHelper.getAttrInt(e, "multiplier_used"));
    }

    public String getDate() {
        return date;
    }

    public String getState() {
        return state;
    }

    public String getType() {
        return type;
    }

    public Integer getGateId() {
        return gateId;
    }

    public Integer getDuplicate() {
        return duplicate;
    }

    public Integer getPartId() {
        return partId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public Integer getAmount() {
        return amount;
    }

    public Integer getCurrent() {
        return current;
    }

    public Integer getTotal() {
        return total;
    }

    public Integer getMultiplierUsed() {
        return multiplierUsed;
    }

    @Override
    public String toString() {
        return "Item{" +
                "date='" + date + '\'' +
                ", state='" + state + '\'' +
                ", type='" + type + '\'' +
                ", gateId=" + gateId +
                ", duplicate=" + duplicate +
                ", partId=" + partId +
                ", itemId=" + itemId +
                ", amount=" + amount +
                ", current=" + current +
                ", total=" + total +
                ", multiplierUsed=" + multiplierUsed +
                '}';
    }
}