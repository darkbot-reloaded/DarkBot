package com.github.manolo8.darkbot.backpage.entities.galaxy;

import com.github.manolo8.darkbot.utils.XmlHelper;
import org.w3c.dom.Element;

/**
 * Make sure to handle nulls
 */
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

    Item update(Element e) {
        this.date           = e.getAttribute("date");
        this.state          = e.getAttribute("state");
        this.type           = e.getAttribute("type");
        this.gateId         = XmlHelper.attrToInt(e, "gate_id");
        this.duplicate      = XmlHelper.attrToInt(e, "duplicate");
        this.partId         = XmlHelper.attrToInt(e, "part_id");
        this.itemId         = XmlHelper.attrToInt(e, "item_id");
        this.amount         = XmlHelper.attrToInt(e, "amount");
        this.current        = XmlHelper.attrToInt(e, "current");
        this.total          = XmlHelper.attrToInt(e, "total");
        this.multiplierUsed = XmlHelper.attrToInt(e, "multiplier_used");

        return this;
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