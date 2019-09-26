package com.github.manolo8.darkbot.utils;

import org.dom4j.Element;

import java.util.stream.Stream;

public class XmlHelper {

    public static Integer getAttrInt(Element e, String attr) {
        String value = e.attributeValue(attr);
        if (value == null || value.isEmpty()) return null;
        return Integer.parseInt(value);
    }

    public static Integer getValueInt(Element e, String child) {
        String value = e.elementText(child);
        if (value == null || value.isEmpty()) return null;
        else return Integer.parseInt(value);
    }

    public static boolean hasChild(Element e, String child) {
        return e.elementIterator(child).hasNext();
    }

    public static Stream<Element> childrenOf(Element e, String child) {
        return e.elementIterator(child).next().elements().stream();
    }
}