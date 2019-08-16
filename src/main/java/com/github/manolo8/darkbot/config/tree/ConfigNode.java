package com.github.manolo8.darkbot.config.tree;

import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.config.types.Options;
import com.github.manolo8.darkbot.config.types.suppliers.OptionList;
import com.github.manolo8.darkbot.gui.tree.components.JActionTable;
import com.github.manolo8.darkbot.gui.tree.components.JBoxInfoTable;
import com.github.manolo8.darkbot.gui.tree.components.JFileOpener;
import com.github.manolo8.darkbot.gui.tree.components.JListField;
import com.github.manolo8.darkbot.gui.tree.components.JNpcInfoTable;
import com.github.manolo8.darkbot.gui.tree.components.JPercentField;
import com.github.manolo8.darkbot.gui.utils.Strings;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import javax.swing.tree.TreeNode;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public abstract class ConfigNode {
    private final Parent parent;
    public final String name;
    public final String description;

    ConfigNode(Parent parent, String name, String description) {
        this.parent = parent;
        this.name = name;
        this.description = description;
    }

    public int getDepth() {
        ConfigNode node = this;
        int levels = 0;
        while((node = node.parent) != null) levels++;
        return levels;
    }

    public String getLongestSibling() {
        return parent.longestChild;
    }

    ConfigNode(Parent parent, Option option) {
        this(parent, option.value(), option.description());
    }

    static ConfigNode.Parent rootingFrom(Parent parent, String name, Object root) {
        return new Parent(parent, name, "").addChildren(p -> Arrays.stream(root.getClass().getFields())
                .filter(f -> f.getAnnotation(Option.class) != null)
                .map(f -> ConfigNode.of(p, new ConfigField(root, f))).toArray(ConfigNode[]::new));
    }

    private static ConfigNode of(Parent parent, ConfigField field) {
        Field[] children = Arrays.stream(field.field.getType().getDeclaredFields())
                .filter(f -> f.getAnnotation(Option.class) != null)
                .toArray(Field[]::new);

        if (children.length == 0) return new Leaf(parent, field);
        Object obj = field.get();
        Option op = field.field.getAnnotation(Option.class);
        return new Parent(parent, op.value(), op.description()).addChildren(p -> Arrays.stream(children)
                .map(f -> ConfigNode.of(p, new ConfigField(obj, f))).toArray(ConfigNode[]::new));

    }
    
    static class Parent extends ConfigNode {
        ConfigNode[] children;
        String longestChild;

        Parent(Parent parent, String name, String description) {
            super(parent, name, description);
        }

        Parent addChildren(Function<Parent, ConfigNode[]> children) {
            this.children = children.apply(this);
            longestChild = Arrays.stream(this.children)
                    .filter(c -> c instanceof Leaf)
                    .map(c -> c.name)
                    .filter(name -> !name.isEmpty())
                    .max(Comparator.comparingInt(String::length)).orElse(null);
            return this;
        }
    }
    
    public static class Leaf extends ConfigNode {
        public final ConfigField field;

        Leaf(Parent parent, ConfigField field) {
            super(parent, field.field.getAnnotation(Option.class));
            this.field = field;
        }

        @Override
        public String toString() {
            Object obj = field.get();
            if (field.getEditor() == JPercentField.class) return DecimalFormat.getPercentInstance().format(obj);
            if (field.getEditor() == JListField.class) {
                OptionList options = ReflectionUtils.createInstance(field.field.getAnnotation(Options.class).value());
                return options.getText(obj);
            }
            if (field.getEditor() == JBoxInfoTable.class) return "Box infos (" + ((Map) obj).size() + ")";
            if (field.getEditor() == JNpcInfoTable.class) return "Npc infos (" + ((Map) obj).size() + ")";
            if (field.getEditor() == JActionTable.class)  return "Action infos (" + ((Map) obj).size() + ")";
            if (field.getEditor() == JFileOpener.class) return Strings.fileName((String) obj);
            return Objects.toString(obj, "(unset)");
        }

    }
}

