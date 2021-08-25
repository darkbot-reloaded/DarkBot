package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.config.tree.ConfigSetting;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class DefaultHandler<T> implements ValueHandler<T> {

    protected final @Nullable Field field;

    public DefaultHandler() {
        this(null);
    }

    public DefaultHandler(@Nullable Field field) {
        this.field = field;
    }

    @Override
    public T validate(T t) {
        return t;
    }

    @Override
    public void update(ConfigSetting<T> setting, T t) {
        ConfigSetting.Parent<?> parent = setting.getParent();
        if (parent != null) {
            Object parentObj = parent.getValue();
            if (parentObj != null) ReflectionUtils.set(field, parentObj, t);
        }

        if (setting instanceof ConfigSetting.Parent) {
            ConfigSetting.Parent<T> current = (ConfigSetting.Parent<T>) setting;
            // TODO: update children field values
            current.getChildren()
                    .forEach((key, child) -> child.getHandler());
        }
    }
}
