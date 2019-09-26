package com.github.manolo8.darkbot.backpage.entities.galaxy;

import com.github.manolo8.darkbot.utils.XmlHelper;
import org.dom4j.Element;

public class Multiplier {
    private String mode;
    private Integer state;
    private Integer value;

    public Multiplier(String mode, Integer state, Integer value) {
        this.mode = mode;
        this.state = state;
        this.value = value;
    }

    public Multiplier(Element e) {
        this(e.attributeValue("mode"), XmlHelper.getAttrInt(e, "state"), XmlHelper.getAttrInt(e, "value"));
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

    @Override
    public String toString() {
        return "Multiplier{" +
                "mode='" + mode + '\'' +
                ", state=" + state +
                ", value=" + value +
                '}';
    }
}
