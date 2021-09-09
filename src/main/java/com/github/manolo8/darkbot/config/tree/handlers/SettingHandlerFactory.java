package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.types.Length;
import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.config.types.Placeholder;
import eu.darkbot.api.API;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.annotations.Percentage;
import eu.darkbot.api.config.annotations.Table;
import eu.darkbot.api.config.annotations.Tag;
import eu.darkbot.api.config.annotations.Text;
import eu.darkbot.api.config.types.PercentRange;
import eu.darkbot.api.config.types.PlayerTag;
import eu.darkbot.api.config.util.ValueHandler;
import eu.darkbot.api.extensions.PluginInfo;
import eu.darkbot.impl.config.DefaultHandler;

import java.awt.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SettingHandlerFactory implements API.Singleton {

    private final Map<Class<?>, Builder<?>> handlers = new HashMap<>();
    private final Builder<?> fallback = (SimpleBuilder<?>) DefaultHandler::new;

    public SettingHandlerFactory(PluginAPI api) {
        addHandlers(new HandlerBuilder<java.lang.Number>()
                .addHandler(Percentage.class, NumberHandler::ofPercentage)
                .addHandler(eu.darkbot.api.config.annotations.Number.class, NumberHandler::of)
                .addHandler(Num.class, NumberHandler::ofLegacy),
                int.class, double.class, Integer.class, Double.class);

        addHandlers(new HandlerBuilder<String>(StringHandler::of)
                .addHandler(Length.class, StringHandler::ofLegacy)
                .addHandler(Placeholder.class, StringHandler::ofLegacy), String.class);

        addHandlers(new HandlerBuilder<PlayerTag>(PlayerTagHandler::fallback)
                        .addHandler(Tag.class, PlayerTagHandler::of)
                        .addHandler(com.github.manolo8.darkbot.config.types.Tag.class, PlayerTagHandler::ofLegacy),
                PlayerTag.class, com.github.manolo8.darkbot.config.PlayerTag.class);

        addHandlers(ColorHandler::of, Color.class);
        addHandlers(RangeHandler::of, PercentRange.class, Config.PercentRange.class);
        addHandlers(new HandlerBuilder<>()
                .addHandler(Table.class, (f, i) -> TableHandler.of(f, i, api)), Map.class);
    }

    public boolean hasHandler(Class<?> type) {
        return handlers.containsKey(type);
    }

    public <T> ValueHandler<T> getHandler(Field field, PluginInfo namespace) {
        if (field == null) return new DefaultHandler<>();
        //noinspection unchecked
        return (ValueHandler<T>) handlers.getOrDefault(field.getType(), fallback).apply(field, namespace);
    }

    @SafeVarargs
    private final <B extends Builder<T>, T> void addHandlers(B builder, Class<? extends T>... classes) {
        for (Class<?> type : classes) {
            handlers.put(type, builder);
        }
    }

    @SafeVarargs
    private final <B extends SimpleBuilder<T>, T> void addHandlers(B builder, Class<? extends T>... classes) {
        addHandlers((Builder<T>) builder, classes);
    }

    @FunctionalInterface
    private interface Builder<T> {
        ValueHandler<T> apply(Field field, PluginInfo pluginInfo);
    }

    @FunctionalInterface
    private interface SimpleBuilder<T> extends Builder<T> {
        default ValueHandler<T> apply(Field field, PluginInfo pluginInfo) {
            return apply(field);
        }

        ValueHandler<T> apply(Field field);
    }

    private class HandlerBuilder<T> implements Builder<T> {
        private final Map<Class<? extends Annotation>, Builder<T>> handlers = new HashMap<>();
        private final Builder<T> builderFallback;

        @SuppressWarnings("unchecked")
        public HandlerBuilder() {
            this((Builder<T>) fallback);
        }

        public HandlerBuilder(Builder<T> fallback) {
            this.builderFallback = fallback;
        }

        public HandlerBuilder(SimpleBuilder<T> fallback) {
            this((Builder<T>) fallback);
        }

        public <A extends Annotation> HandlerBuilder<T> addHandler(Class<A> ann, Builder<T> val) {
            this.handlers.put(ann, val);
            return this;
        }

        public <A extends Annotation> HandlerBuilder<T> addHandler(Class<A> ann, SimpleBuilder<T> val) {
            return addHandler(ann, (Builder<T>) val);
        }

        public ValueHandler<T> apply(Field field, PluginInfo namespace) {
            for (Map.Entry<Class<? extends Annotation>, Builder<T>> entry : handlers.entrySet()) {
                if (!field.isAnnotationPresent(entry.getKey())) continue;
                return entry.getValue().apply(field, namespace);
            }

            return builderFallback.apply(field, namespace);
        }
    }

}
