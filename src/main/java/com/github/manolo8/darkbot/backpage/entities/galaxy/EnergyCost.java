package com.github.manolo8.darkbot.backpage.entities.galaxy;

import org.dom4j.Element;

public class EnergyCost {
    private String mode;
    private Integer value;

    public EnergyCost(String mode, Integer value) {
        this.mode = mode;
        this.value = value;
    }

    public EnergyCost(Element e) {
        this(e.attributeValue("mode"), Integer.parseInt(e.getText()));
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