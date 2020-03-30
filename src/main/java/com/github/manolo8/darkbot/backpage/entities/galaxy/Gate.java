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
    private Integer multiplier = -1;

    void update(Element e) {
        setState      (e.getAttribute("state"));
        setTotal      (XmlHelper.attrToInt(e, "total"));
        setCurrent    (XmlHelper.attrToInt(e, "current"));
        setId         (XmlHelper.attrToInt(e, "id"));
        setId         (XmlHelper.attrToInt(e, "gate_id"));
        setPrepared   (XmlHelper.attrToInt(e, "prepared"));
        setTotalWave  (XmlHelper.attrToInt(e, "totalWave"));
        setCurrentWave(XmlHelper.attrToInt(e, "currentWave"));
        setLivesLeft  (XmlHelper.attrToInt(e, "livesLeft"));
        setLifePrice  (XmlHelper.attrToInt(e, "lifePrice"));
    }

    public boolean shouldStopSpinning() {
        return getLivesLeft() > 0 && isFinished();
    }

    public boolean isFinished() {
        return getCurrent().equals(getTotal());
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

    /**
     * @return -1 if was never assigned. Only for KRONOS actually
     */
    public Integer getMultiplier() {
        return multiplier;
    }

    void onGatePrepare() {
        this.state    = "on_progress";
        this.current  = 0;
        this.prepared = 1;
    }

    void setMultiplier(Element multiplier) {
        this.multiplier = XmlHelper.attrToInt(multiplier, "value");
    }

    private void setState(String state) {
        if (state != null) this.state = state;
    }

    private void setTotal(Integer total) {
        if (total != null) this.total = total;
    }

    private void setCurrent(Integer current) {
        if (current != null) this.current = current;
    }

    private void setId(Integer id) {
        if (id != null) this.id = id;
    }

    private void setPrepared(Integer prepared) {
        if (prepared != null) this.prepared = prepared;
    }

    private void setTotalWave(Integer totalWave) {
        if (totalWave != null) this.totalWave = totalWave;
    }

    private void setCurrentWave(Integer currentWave) {
        if (currentWave != null) this.currentWave = currentWave;
    }

    private void setLivesLeft(Integer livesLeft) {
        if (livesLeft != null) this.livesLeft = livesLeft;
    }

    private void setLifePrice(Integer lifePrice) {
        if (lifePrice != null) this.lifePrice = lifePrice;
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
                ", multiplier=" + multiplier +
                '}';
    }
}
