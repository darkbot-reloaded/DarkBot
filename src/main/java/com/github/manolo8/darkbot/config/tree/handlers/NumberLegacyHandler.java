package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.config.types.Num;

import java.lang.reflect.Field;

public class NumberLegacyHandler extends NumberHandler {

    public NumberLegacyHandler(Field field) {
        super(field, field.getAnnotation(Num.class).min(),
                field.getAnnotation(Num.class).max(),
                field.getAnnotation(Num.class).step());
    }

}
