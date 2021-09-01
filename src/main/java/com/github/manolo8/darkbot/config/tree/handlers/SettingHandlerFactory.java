package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.config.types.Length;
import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.config.types.Placeholder;
import eu.darkbot.api.API;
import eu.darkbot.api.config.annotations.Percentage;
import eu.darkbot.api.config.annotations.Tag;
import eu.darkbot.api.config.annotations.Text;
import eu.darkbot.api.config.types.PlayerTag;
import eu.darkbot.api.config.util.ValueHandler;
import eu.darkbot.impl.config.DefaultHandler;

import java.awt.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SettingHandlerFactory implements API.Singleton {

    private final Map<Class<?>, Builder<?>> handlers = new HashMap<>();
    private final Builder<?> fallback = DefaultHandler::new;


    public SettingHandlerFactory() {
        addHandlers(new HandlerBuilder<java.lang.Number>()
                .addHandler(Percentage.class, NumberHandler::ofPercentage)
                .addHandler(eu.darkbot.api.config.annotations.Number.class, NumberHandler::of)
                .addHandler(Num.class, NumberHandler::ofLegacy),
                int.class, double.class, Integer.class, Double.class);

        addHandlers(new HandlerBuilder<String>()
                .addHandler(Text.class, StringHandler::of)
                .addHandler(Length.class, StringHandler::ofLegacy)
                .addHandler(Placeholder.class, StringHandler::ofLegacy), String.class);

        addHandlers(new HandlerBuilder<PlayerTag>()
                        .addHandler(Tag.class, PlayerTagHandler::of)
                        .addHandler(com.github.manolo8.darkbot.config.types.Tag.class, PlayerTagHandler::ofLegacy),
                PlayerTag.class);

        addHandlers(ColorHandler::of, Color.class);
    }

    public boolean hasHandler(Class<?> type) {
        return handlers.containsKey(type);
    }

    public <T> ValueHandler<T> getHandler(Field field) {
        if (field == null) return new DefaultHandler<>();
        //noinspection unchecked
        return (ValueHandler<T>) handlers.getOrDefault(field.getType(), fallback).apply(field);
    }

    @SafeVarargs
    private final <B extends Builder<T>, T> void addHandlers(B builder, Class<? extends T>... classes) {
        for (Class<?> type : classes) {
            handlers.put(type, builder);
        }
    }

    private interface Builder<T> extends Function<Field, ValueHandler<T>> {}

    private class HandlerBuilder<T> implements Builder<T> {
        private final Map<Class<? extends Annotation>, Function<Field, ValueHandler<T>>> handlers = new HashMap<>();
        private final Builder<T> builderFallback;

        @SuppressWarnings("unchecked")
        public HandlerBuilder() {
            this((Builder<T>) fallback);
        }

        public HandlerBuilder(Builder<T> fallback) {
            this.builderFallback = fallback;
        }

        public <A extends Annotation> HandlerBuilder<T> addHandler(Class<A> ann, Function<Field, ValueHandler<T>> val) {
            this.handlers.put(ann, val);
            return this;
        }

        public ValueHandler<T> apply(Field field) {
            for (Map.Entry<Class<? extends Annotation>, Function<Field, ValueHandler<T>>> entry : handlers.entrySet()) {
                if (!field.isAnnotationPresent(entry.getKey())) continue;
                return entry.getValue().apply(field);
            }

            return builderFallback.apply(field);
        }
    }

}
