package com.github.manolo8.darkbot.config.tree;

import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.config.types.Options;
import com.github.manolo8.darkbot.gui.tree.components.JBoxInfoTable;
import com.github.manolo8.darkbot.gui.tree.components.JListField;
import com.github.manolo8.darkbot.gui.tree.components.JNpcInfoTable;
import com.github.manolo8.darkbot.gui.tree.components.JPercentField;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public abstract class ConfigNode {
    public final String name;
    public final String description;

    ConfigNode(String name, String description) {
        this.name = name;
        this.description = description;
    }

    ConfigNode(Option option) {
        this(option.value(), option.description());
    }

    static ConfigNode root(Object root) {
        return new Parent("root", "", Arrays.stream(root.getClass().getFields())
                .filter(f -> f.getAnnotation(Option.class) != null)
                .map(f -> ConfigNode.of(new ConfigField(root, f))).toArray(ConfigNode[]::new));
    }

    private static ConfigNode of(ConfigField field) {
        Field[] children = Arrays.stream(field.field.getType().getDeclaredFields())
                .filter(f -> f.getAnnotation(Option.class) != null)
                .toArray(Field[]::new);

        if (children.length == 0) return new Leaf(field);
        Object obj = field.get();
        Option op = field.field.getAnnotation(Option.class);
        return new Parent(op.value(), op.description(), Arrays.stream(children)
                .map(f -> ConfigNode.of(new ConfigField(obj, f))).toArray(ConfigNode[]::new));

    }
    
    static class Parent extends ConfigNode {
        final ConfigNode[] children;

        Parent(String name, String description, ConfigNode[] children) {
            super(name, description);
            this.children = children;
        }
    }
    
    public static class Leaf extends ConfigNode {
        public final ConfigField field;

        Leaf(ConfigField field) {
            super(field.field.getAnnotation(Option.class));
            this.field = field;
        }

        @Override
        public String toString() {
            Object obj = field.get();
            if (field.getEditor() == JPercentField.class) return DecimalFormat.getPercentInstance().format(obj);
            if (field.getEditor() == JListField.class) return ReflectionUtils.createInstance(
                    field.field.getAnnotation(Options.class).value()).get().getText(obj);
            if (field.getEditor() == JBoxInfoTable.class) return "Box infos (" + ((Map) obj).size() + ")";
            if (field.getEditor() == JNpcInfoTable.class) return "Npc infos (" + ((Map) obj).size() + ")";
            return Objects.toString(obj, "");
        }

    }
}

