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
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import com.github.manolo8.darkbot.utils.StringQuery;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public abstract class ConfigNode {

    protected final Parent parent;
    public final String name;
    public final String description;
    public final String key;

    ConfigNode(Parent parent, String name, String description, String key, String field) {
        this.parent = parent;
        this.name = name;
        this.description = !description.isEmpty() ? description : null;
        this.key = !key.isEmpty() ? key
                : parent == null ? field.toLowerCase(Locale.ROOT)
                : parent.key == null ? null
                : parent.key + "." + field.toLowerCase(Locale.ROOT);
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

    // Convenient for searching/filtering
    public String convertToString() {
        String n = I18n.getOrDefault(key, name);
        String desc = I18n.getOrDefault(key + ".desc", description);

        return (key != null ? key.substring(key.lastIndexOf(".") + 1) + " , " : "") +
                (n != null ? n + " , " : "") +
                (desc != null ? desc : "");
    }

    boolean isVisible(StringQuery query) {
        if (match(query)) return true;
        ConfigNode p = this;
        while ((p = p.parent) != null) if (p.match(query)) return true;
        return false;
    }

    protected boolean match(StringQuery query) {
        return query.matches(convertToString());
    }

    static ConfigNode.Parent rootingFrom(Parent parent, String name, Object root, String baseKey) {
        return new Parent(parent, name, "", baseKey, null).addChildren(p -> Arrays.stream(root.getClass().getFields())
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
        return new Parent(parent, op.value(), op.description(), op.key(), field.field.getName())
                .addChildren(p -> Arrays.stream(children)
                .map(f -> ConfigNode.of(p, new ConfigField(obj, f))).toArray(ConfigNode[]::new));

    }
    
    static class Parent extends ConfigNode {
        ConfigNode[] children;
        String longestChild;

        Parent(Parent parent, String name, String description, String key, String field) {
            super(parent, name, description, key, field);
        }

        Parent addChildren(Function<Parent, ConfigNode[]> children) {
            this.children = children.apply(this);
            longestChild = Arrays.stream(this.children)
                    .filter(c -> c instanceof Leaf)
                    .map(c -> I18n.getOrDefault(c.key, c.name))
                    .filter(name -> !name.isEmpty())
                    .max(Comparator.comparingInt(String::length)).orElse(null);
            return this;
        }

        boolean isVisible(StringQuery filter) {
            return super.isVisible(filter) || Arrays.stream(children).anyMatch(n -> n.match(filter));
        }

    }

    public static class Leaf extends ConfigNode {
        public final ConfigField field;

        Leaf(Parent parent, ConfigField field, Option option) {
            super(parent, option.value(), option.description(), option.key(), field.field.getName());
            this.field = field;
        }

        Leaf(Parent parent, ConfigField field) {
            this(parent, field, field.field.getAnnotation(Option.class));
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

