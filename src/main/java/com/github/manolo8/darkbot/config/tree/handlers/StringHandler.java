package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.config.types.Length;
import com.github.manolo8.darkbot.config.types.Placeholder;
import eu.darkbot.api.config.annotations.Text;

import java.lang.reflect.Field;

public class StringHandler implements ValueHandler<String> {

    private final int len;
    private final String placeholder;

    public StringHandler(Field field) {
        Text text = field.getAnnotation(Text.class);
        if (text != null) {
            this.len = text.length();
            this.placeholder = text.placeholder().isEmpty() ? null : text.placeholder();
        } else {
            Length len = field.getAnnotation(Length.class);
            this.len = len == null ? 10 : len.value();

            Placeholder placeholder = field.getAnnotation(Placeholder.class);
            this.placeholder = placeholder == null ? null : placeholder.value();
        }
    }

    public StringHandler(int len, String placeholder) {
        this.len = len;
        this.placeholder = placeholder;
    }

    @Override
    public String validate(String s) {
        return s;
    }

    public int getLen() {
        return len;
    }

    public String getPlaceholder() {
        return placeholder;
    }
}
