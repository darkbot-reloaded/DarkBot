package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.config.types.Num;
import eu.darkbot.api.API;
import eu.darkbot.api.config.util.PlayerTag;

import java.awt.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SettingHandlerFactory implements API.Singleton {

    private final Map<Class<?>, HandlerBuilder<?>> handlers = new HashMap<>();


    public SettingHandlerFactory() {
        addHandlers(new HandlerBuilder<java.lang.Number>(NumberHandler::new)
                .addHandler(Num.class, NumberLegacyHandler::new),
                int.class, double.class, Integer.class, Double.class);

        addHandlers(new HandlerBuilder<>(ColorHandler::new), Color.class);
        addHandlers(new HandlerBuilder<>(StringHandler::new), String.class);
        addHandlers(new HandlerBuilder<>(PlayerTagHandler::new), PlayerTag.class);
    }

    public boolean hasHandler(Class<?> type) {
        return handlers.containsKey(type);
    }

    public <T> ValueHandler<? extends T> getHandler(Field field) {
        @SuppressWarnings("unchecked")
        HandlerBuilder<? extends T> builder = (HandlerBuilder<? extends T>) handlers.get(field.getType());
        return builder != null ? builder.getHandler(field) : null;

    }

    @SafeVarargs
    private final <T> void addHandlers(HandlerBuilder<T> builder, Class<? extends T>... classes) {
        for (Class<? extends T> type : classes) {
            handlers.put(type, builder);
        }
    }

    private static class HandlerBuilder<T> {
        private final Map<Class<? extends Annotation>, Function<Field, ValueHandler<T>>> handlers = new HashMap<>();
        private final Function<Field, ValueHandler<T>> fallback;

        public HandlerBuilder(Function<Field, ValueHandler<T>> fallback) {
            this.fallback = fallback;
        }

        public <A extends Annotation> HandlerBuilder<T> addHandler(Class<A> ann, Function<Field, ValueHandler<T>> val) {
            this.handlers.put(ann, val);
            return this;
        }

        public ValueHandler<T> getHandler(Field field) {
            for (Map.Entry<Class<? extends Annotation>, Function<Field, ValueHandler<T>>> entry : handlers.entrySet()) {
                if (!field.isAnnotationPresent(entry.getKey())) continue;
                return entry.getValue().apply(field);
            }

            return fallback != null ? fallback.apply(field) : null;
        }
    }

}
