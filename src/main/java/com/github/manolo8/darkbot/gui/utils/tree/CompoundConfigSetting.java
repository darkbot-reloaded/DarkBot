package com.github.manolo8.darkbot.gui.utils.tree;

import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.ValueHandler;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Creates a config node with a base, and a set of appended nodes at the end
 */
public class CompoundConfigSetting<T> implements ConfigSetting.Parent<T> {

    private final ConfigSetting.Parent<T> base;
    private final Map<String, ConfigSetting<?>> remapped;

    public CompoundConfigSetting(ConfigSetting.Parent<T> base,
                                 ConfigSetting<?>... appended) {
        this.base = base;
        this.remapped = new LinkedHashMap<>();
        setAppended(appended);
    }

    public void setAppended(ConfigSetting<?>... appended) {
        remapped.clear();
        remapped.putAll(base.getChildren());

        if (appended == null) return;
        for (ConfigSetting<?> child : appended) {
            if (child == null) continue;
            String baseKey = child.getKey();
            // If key isn't configured for this root, generate one from name
            if (baseKey.isEmpty() || baseKey.equals("config"))
                baseKey = child.getName().toLowerCase(Locale.ROOT).replace(" ", "_");
            // If the key isn't unique, append _ at the end until it is
            while (remapped.containsKey(baseKey)) baseKey += "_";
            remapped.put(baseKey, child);
        }
    }

    @Override
    public Map<String, ConfigSetting<?>> getChildren() {
        return remapped;
    }

    @Override
    public @Nullable Parent<?> getParent() {
        return base.getParent();
    }

    @Override
    public String getKey() {
        return base.getKey();
    }

    @Override
    public String getName() {
        return base.getName();
    }

    @Override
    public @Nullable String getDescription() {
        return base.getDescription();
    }

    @Override
    public Class<T> getType() {
        return base.getType();
    }

    @Override
    public T getValue() {
        return base.getValue();
    }

    @Override
    public void setValue(T t) {
        base.setValue(t);
    }

    @Override
    public void addListener(Consumer<T> consumer) {
        base.addListener(consumer);
    }

    @Override
    public void removeListener(Consumer<T> consumer) {
        base.removeListener(consumer);
    }

    @Override
    public ValueHandler<T> getHandler() {
        return base.getHandler();
    }

}
