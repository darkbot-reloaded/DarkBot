package com.github.manolo8.darkbot.backpage.entities.galaxy;

import com.github.manolo8.darkbot.utils.XmlHelper;
import org.w3c.dom.Element;

public class Multiplier {
    private String mode;
    private Integer state;
    private Integer value;

    Multiplier update(Element e) {
        this.mode = e.getAttribute("mode");
        this.state = XmlHelper.attrToInt(e, "state");
        this.value = XmlHelper.attrToInt(e, "value");
        return this;
    }

    public String getMode() {
        return mode;
    }

    public Integer getState() {
        return state;
    }

    public Integer getValue() {
        return value;
    }

    boolean alreadyInList(String mode) {
        return this.mode.equals(mode);
    }

    @Override
    public String toString() {
        return "Multiplier{" +
                "mode='" + mode + '\'' +
                ", state=" + state +
                ", value=" + value +
                '}';
    }
}
