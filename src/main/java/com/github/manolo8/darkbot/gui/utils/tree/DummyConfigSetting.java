package com.github.manolo8.darkbot.gui.utils.tree;

import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.ValueHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Partially implements a parent config setting, leaving only getChildren missing implementation
 * @param <T>
 */
public abstract class DummyConfigSetting<T> implements ConfigSetting.Parent<T> {

    private final String key;
    private final String name;
    private final ConfigSetting.Parent<?> parent;

    DummyConfigSetting(String name, ConfigSetting.Parent<?> parent) {
        this.key = name.toLowerCase(Locale.ROOT).replace(" ", "_");
        this.name = name;
        this.parent = parent;
    }

    @Override
    public @Nullable Parent<?> getParent() {
        return parent;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Nullable String getDescription() {
        return null;
    }

    @Override
    public Class<T> getType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(Consumer<T> consumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeListener(Consumer<T> consumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueHandler<T> getHandler() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V> V getOrCreateMetadata(String key, Supplier<V> builder) {
        return builder.get();
    }
}
