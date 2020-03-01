package com.github.manolo8.darkbot.backpage.entities.galaxy;

import com.github.manolo8.darkbot.utils.XmlHelper;
import org.w3c.dom.Element;

public class EnergyCost {
    private String mode;
    private Integer value;

    public void update(Element e) {
        this.mode = e.getAttribute("mode");
        this.value = XmlHelper.valueToInt(e);
    }

    public String getMode() {
        return mode;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "EnergyCost{" +
                "mode='" + mode + '\'' +
                ", value=" + value +
                '}';
    }
}