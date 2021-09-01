package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.config.types.Length;
import com.github.manolo8.darkbot.config.types.Placeholder;
import eu.darkbot.api.config.annotations.Text;
import eu.darkbot.impl.config.DefaultHandler;

import java.lang.reflect.Field;

public class StringHandler extends DefaultHandler<String> {

    public static StringHandler of(Field field) {
        int length = 10;
        String placeholder = null;
        Text text = field.getAnnotation(Text.class);
        if (text != null) {
            length = text.length();
            placeholder = text.placeholder().isEmpty() ? null : text.placeholder();
        }

        return new StringHandler(field, length, placeholder);
    }

    public static StringHandler ofLegacy(Field field) {
        Length len = field.getAnnotation(Length.class);
        int length = len == null ? 10 : len.value();

        Placeholder ph = field.getAnnotation(Placeholder.class);
        String placeholder = ph == null ? null : ph.value();

        return new StringHandler(field, length, placeholder);
    }

    public StringHandler(int len, String placeholder) {
        this(null, len, placeholder);
    }

    public StringHandler(Field field, int len, String placeholder) {
        super(field);
        metadata.put("length", len);
        metadata.put("placeholder", placeholder);
    }

}
