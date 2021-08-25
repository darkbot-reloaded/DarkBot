package com.github.manolo8.darkbot.config.tree;


import com.github.manolo8.darkbot.config.tree.handlers.ValueHandler;
import org.jetbrains.annotations.Nullable;
import org.omg.CORBA.Object;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a setting that can be configured by the user
 */
public class ConfigSetting<T> {

    private final @Nullable Parent<?> parent;
    private final String key;
    private final String name;
    private final String description;

    private final Class<T> type;
    private final ValueHandler<T> handler;
    private T value;

    private final List<Consumer<T>> listeners = new ArrayList<>();

    public ConfigSetting(@Nullable Parent<?> parent,
                         String key, String name, String description,
                         Class<T> type,
                         ValueHandler<T> handler) {
        this.parent = parent;
        this.key = key;
        this.name = name;
        this.description = description;
        this.type = type;
        this.handler = handler;
    }

    public @Nullable Parent<?> getParent() {
        return parent;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Class<T> getType() {
        return type;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        value = handler.validate(value);
        if (this.value != value) {
            this.value = value;
            handler.update(this, value);
        }
        listeners.forEach(l -> l.accept(this.value));
    }

    public void addListener(Consumer<T> listener) {
        if (!this.listeners.contains(listener))
            this.listeners.add(listener);
    }

    public <H extends ValueHandler<? extends T>> H getHandler() {
        //noinspection unchecked
        return (H) handler;
    }

    public static abstract class Parent<T> extends ConfigSetting<T> {
        private final Map<String, ConfigSetting<?>> children;

        public Parent(Parent<?> parent,
                      String key, String name, String description,
                      Class<T> type,
                      ValueHandler<T> handler,
                      Function<Parent<?>, Map<String, ConfigSetting<?>>> children) {
            super(parent, key, name, description, type, handler);
            this.children = children.apply(this);
        }

        public Map<String, ConfigSetting<?>> getChildren() {
            return children;
        }
    }

    public static class Root<T> extends Parent<T> {
        public Root(String key, String name, String description,
                    Class<T> type,
                    ValueHandler<T> handler,
                    Function<Parent<?>, Map<String, ConfigSetting<?>>> children) {
            super(null, key, name, description, type, handler, children);
        }
    }

    public static class Intermediate<T> extends Parent<T> {
        public Intermediate(Parent<?> parent,
                            String key, String name, String description,
                            Class<T> type,
                            ValueHandler<T> handler,
                            Function<Parent<?>, Map<String, ConfigSetting<?>>> children) {
            super(parent, key, name, description, type, handler, children);
        }
    }

    public static class Leaf<T> extends ConfigSetting<T> {

        public Leaf(Parent<?> parent, String key, String name, String description, Class<T> type,
                    ValueHandler<T> handler) {
            super(parent, key, name, description, type, handler);
        }

    }

}

