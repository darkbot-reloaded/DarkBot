package com.github.manolo8.darkbot.utils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.stream.IntStream;
import java.util.stream.Stream;

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
        if (e == null) return null;
        String value = e.getTextContent();
        return value == null || value.isEmpty() ? null : Integer.parseInt(value);
    }

    public static Stream<Element> stream(NodeList list) {
        return IntStream.range(0, list.getLength()).mapToObj(i -> (Element) list.item(i));
    }

}