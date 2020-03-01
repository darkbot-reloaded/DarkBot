package com.github.manolo8.darkbot.utils;

import org.w3c.dom.Element;

public class XmlHelper {

    public static boolean hasAnyElement(Element e, String name) {
        return e.getElementsByTagName(name).getLength() > 0;
    }

    public static Element getElement(Element e, String name) {
        return (Element) e.getElementsByTagName(name).item(0);
    }

    public static Integer attrToInt(Element e, String attr) {
        String value = e.getAttribute(attr);
        return value == null || value.isEmpty() ? null : Integer.parseInt(value);
    }

    public static Integer valueToInt(Element e, String name) {
        return valueToInt(getElement(e, name));
    }

    public static Integer valueToInt(Element e) {
        String value = e.getTextContent();
        return value == null || value.isEmpty() ? null : Integer.parseInt(value);
    }
}