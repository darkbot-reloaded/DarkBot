package com.github.manolo8.darkbot.backpage.entities.galaxy;

import com.github.manolo8.darkbot.utils.XmlHelper;
import org.dom4j.Element;

public class Gate {
    private String state;

    private Integer total;
    private Integer current;
    private Integer id;
    private Integer prepared;
    private Integer totalWave;
    private Integer currentWave;
    private Integer livesLeft;
    private Integer lifePrice;

    public Gate(String state, Integer total, Integer current, Integer id, Integer prepared, Integer totalWave, Integer currentWave, Integer livesLeft, Integer lifePrice) {
        this.state = state;
        this.total = total;
        this.current = current;
        this.id = id;
        this.prepared = prepared;
        this.totalWave = totalWave;
        this.currentWave = currentWave;
        this.livesLeft = livesLeft;
        this.lifePrice = lifePrice;
    }

    public Gate(Element e) {
        this(e.attributeValue("state"), XmlHelper.getAttrInt(e, "total"), XmlHelper.getAttrInt(e, "current"),
                XmlHelper.getAttrInt(e, "id"), XmlHelper.getAttrInt(e, "prepared"),
                XmlHelper.getAttrInt(e, "totalWave"), XmlHelper.getAttrInt(e, "currentWave"),
                XmlHelper.getAttrInt(e, "livesLeft"), XmlHelper.getAttrInt(e, "lifePrice"));
    }

    public String getState() {
        return state;
    }

    public Integer getTotal() {
        return total;
    }

    public Integer getCurrent() {
        return current;
    }

    public Integer getId() {
        return id;
    }

    public Integer getPrepared() {
        return prepared;
    }

    public Integer getTotalWave() {
        return totalWave;
    }

    public Integer getCurrentWave() {
        return currentWave;
    }

    public Integer getLivesLeft() {
        return livesLeft;
    }

    public Integer getLifePrice() {
        return lifePrice;
    }

    @Override
    public String toString() {
        return "Gate{" +
                "state='" + state + '\'' +
                ", total=" + total +
                ", current=" + current +
                ", id=" + id +
                ", prepared=" + prepared +
                ", totalWave=" + totalWave +
                ", currentWave=" + currentWave +
                ", livesLeft=" + livesLeft +
                ", lifePrice=" + lifePrice +
                '}';
    }
}
