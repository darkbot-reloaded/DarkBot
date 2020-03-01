package com.github.manolo8.darkbot.backpage.entities.galaxy;

import com.github.manolo8.darkbot.utils.XmlHelper;
import org.w3c.dom.Element;

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

    Gate update(Element e) {
        this.state = e.getAttribute("state");
        this.total = XmlHelper.attrToInt(e, "total");
        this.current = XmlHelper.attrToInt(e, "current");
        this.id = XmlHelper.attrToInt(e, "id");
        this.prepared = XmlHelper.attrToInt(e, "prepared");
        this.totalWave = XmlHelper.attrToInt(e, "totalWave");
        this.currentWave = XmlHelper.attrToInt(e, "currentWave");
        this.livesLeft = XmlHelper.attrToInt(e, "livesLeft");
        this.lifePrice = XmlHelper.attrToInt(e, "lifePrice");

        return this;
    }

    boolean alreadyInList(Integer id) {
        return this.id.equals(id);
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
