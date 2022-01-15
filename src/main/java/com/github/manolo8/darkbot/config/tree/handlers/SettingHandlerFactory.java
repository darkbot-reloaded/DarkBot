package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.types.Length;
import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.config.types.Placeholder;
import eu.darkbot.api.API;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.annotations.Dropdown;
import eu.darkbot.api.config.annotations.Percentage;
import eu.darkbot.api.config.annotations.Table;
import eu.darkbot.api.config.annotations.Tag;
import eu.darkbot.api.config.types.PercentRange;
import eu.darkbot.api.config.types.PlayerTag;
import eu.darkbot.api.config.util.ValueHandler;
import eu.darkbot.api.extensions.PluginInfo;
import eu.darkbot.impl.config.DefaultHandler;

import java.awt.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SettingHandlerFactory implements API.Singleton {

    private final Parent root = new Parent(null);

    public SettingHandlerFactory(PluginAPI api) {
        root.add(new Parent(type(int.class, double.class, Integer.class, Double.class))
                        .add(annot(Percentage.class), NumberHandler::ofPercentage)
                        .add(annot(eu.darkbot.api.config.annotations.Number.class), NumberHandler::of)
                        .add(annot(Num.class), NumberHandler::ofLegacy))
                .add(annot(Dropdown.class), (f, i) -> DropdownHandler.of(f, i, api))
                .add(new Parent(type(String.class))
                        .add(annot(Length.class, Placeholder.class), StringHandler::ofLegacy)
                        .add(f -> true, StringHandler::of))
                .add(new Parent(type(PlayerTag.class, com.github.manolo8.darkbot.config.PlayerTag.class))
                        .add(annot(Tag.class), PlayerTagHandler::of)
                        .add(annot(com.github.manolo8.darkbot.config.types.Tag.class), PlayerTagHandler::ofLegacy)
                        .add(f -> true, PlayerTagHandler::fallback))
                .add(type(Color.class), ColorHandler::of)
                .add(type(PercentRange.class, Config.PercentRange.class), RangeHandler::of)
                .add(annot(Table.class), (f, i) -> TableHandler.of(f, i, api));
    }

    public boolean hasHandler(Field field) {
        return field != null && findHandler(root, field) != null;
    }

    public <T> ValueHandler<T> getHandler(Field field, PluginInfo namespace) {
        if (field == null) return new DefaultHandler<>();

        Leaf<T> node = findHandler(root, field);
        if (node != null) return node.builder.apply(field, namespace);
        else return new DefaultHandler<>(field);
    }

    private <T> Leaf<T> findHandler(Parent parent, Field field) {
        for (Node child : parent.children) {
            if (!child.predicate.test(field)) continue;
            if (child instanceof Leaf) return (Leaf<T>) child;
            else if (child instanceof Parent) {
                Leaf<T> found = findHandler((Parent) child, field);
                if (found != null) return found;
            }
        }
        return null;
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

    private Predicate<Field> type(Class<?>... classes) {
        return f -> {
            Class<?> type = f.getType();
            for (Class<?> c : classes) if (c.isAssignableFrom(type)) return true;
            return false;
        };
    }

    @SafeVarargs
    private final Predicate<Field> annot(Class<? extends Annotation>... classes) {
        return f -> {
            for (Class<? extends Annotation> c : classes) if (f.isAnnotationPresent(c)) return true;
            return false;
        };
    }


    private static abstract class Node {
        final Predicate<Field> predicate;

        public Node(Predicate<Field> predicate) {
            this.predicate = predicate;
        }

    }

    private static class Parent extends Node {
        private final List<Node> children;

        public Parent(Predicate<Field> predicate) {
            this(predicate, new ArrayList<>());
        }

        public Parent(Predicate<Field> predicate, List<Node> children) {
            super(predicate);
            this.children = children;
        }

        public <T> Parent add(Node node) {
            this.children.add(node);
            return this;
        }

        public <T> Parent add(Predicate<Field> condition, Builder<T> b) {
            children.add(new Leaf<T>(condition, b));
            return this;
        }

        public <T> Parent add(Predicate<Field> condition, SimpleBuilder<T> b) {
            children.add(new Leaf<T>(condition, b));
            return this;
        }

        public Parent add(Predicate<Field> condition, List<Node> children) {
            children.add(new Parent(condition, children));
            return this;
        }
    }

    private static class Leaf<T> extends Node {
        final Builder<T> builder;

        public Leaf(Predicate<Field> predicate, Builder<T> builder) {
            super(predicate);
            this.builder = builder;
        }

        public Leaf(Predicate<Field> predicate, SimpleBuilder<T> builder) {
            super(predicate);
            this.builder = builder;
        }
    }

}
