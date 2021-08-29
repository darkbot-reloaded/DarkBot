package com.github.manolo8.darkbot.config.tree.handlers;

import eu.darkbot.impl.config.DefaultHandler;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class FieldDefaultHandler<T> extends DefaultHandler<T> implements FieldHolder {

    public FieldDefaultHandler(@Nullable Field field) {
        super(field);
    }

    public FieldDefaultHandler() {
    }

    public Field getField() {
        return field;
    }

}
