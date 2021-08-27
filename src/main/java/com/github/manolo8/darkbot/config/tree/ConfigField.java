package com.github.manolo8.darkbot.config.tree;


import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.tree.handlers.FieldDefaultHandler;
import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import eu.darkbot.api.config.ConfigSetting;

import java.lang.reflect.Field;

/**
 * Represents a java field in an object. Can act as getter/setter.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ConfigField {
    private final ConfigSetting config;
    public final Field field;

    public ConfigField(ConfigSetting<?> config) {
        this.config = config;
        FieldDefaultHandler<?> fdh = config.getHandler(FieldDefaultHandler.class);
        field = fdh != null ? fdh.getField() : null;
    }

    public boolean isPrimitive() {
        return field.getType().isPrimitive();
    }

    public <T> T get() {
        return (T) config.getValue();
    }

    public void set(Object value) {
        config.setValue(value);
        ConfigEntity.changed();
    }

    public Object getParent() {
        ConfigSetting.Parent<?> p = config.getParent();
        return p == null ? null : p.getValue();
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
