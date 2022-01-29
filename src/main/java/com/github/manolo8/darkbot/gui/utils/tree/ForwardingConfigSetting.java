package com.github.manolo8.darkbot.gui.utils.tree;

import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.ValueHandler;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class ForwardingConfigSetting<T> implements ConfigSetting<T> {

    public abstract ConfigSetting<T> delegate();

    @Override
    public @Nullable Parent<?> getParent() {
        return delegate().getParent();
    }

    @Override
    public String getKey() {
        return delegate().getKey();
    }

    @Override
    public String getName() {
        return delegate().getName();
    }

    @Override
    public @Nullable String getDescription() {
        return delegate().getDescription();
    }

    @Override
    public Class<T> getType() {
        return delegate().getType();
    }

    @Override
    public T getValue() {
        return delegate().getValue();
    }

    @Override
    public void setValue(T value) {
        delegate().setValue(value);
    }

    @Override
    public void addListener(Consumer<T> listener) {
        delegate().addListener(listener);
    }

    @Override
    public void removeListener(Consumer<T> listener) {
        delegate().removeListener(listener);
    }

    @Override
    public ValueHandler<T> getHandler() {
        return delegate().getHandler();
    }
}
