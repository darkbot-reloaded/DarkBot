package com.github.manolo8.darkbot.config.tree;


import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;

import java.lang.reflect.Field;

/**
 * Represents a java field in an object. Can act as getter/setter.
 */
public class ConfigField {
    public final Object parent;
    public final Field field;

    ConfigField(Object parent, Field field) {
        this.parent = parent;
        this.field = field;
    }

    public boolean isPrimitive() {
        return field.getType().isPrimitive();
    }

    public <T> T get() {
        try {
            return (T) field.get(parent);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void set(Object value) {
        try {
            field.set(parent, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        ConfigEntity.changed();
    }

    public Class<? extends OptionEditor> getEditor() {
        Editor editor = field.getAnnotation(Editor.class);
        return editor == null ? null : editor.value();
    }

    public boolean isSharedEditor() {
        Editor editor = field.getAnnotation(Editor.class);
        return editor != null && editor.shared();
    }

}
