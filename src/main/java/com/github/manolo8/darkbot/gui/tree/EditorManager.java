package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.PlayerTag;
import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.tree.components.JBoolField;
import com.github.manolo8.darkbot.gui.tree.components.JCharField;
import com.github.manolo8.darkbot.gui.tree.components.JColorField;
import com.github.manolo8.darkbot.gui.tree.components.JConditionField;
import com.github.manolo8.darkbot.gui.tree.components.JFontField;
import com.github.manolo8.darkbot.gui.tree.components.JLabelField;
import com.github.manolo8.darkbot.gui.tree.components.JNumberField;
import com.github.manolo8.darkbot.gui.tree.components.JPlayerTagField;
import com.github.manolo8.darkbot.gui.tree.components.JRangeField;
import com.github.manolo8.darkbot.gui.tree.components.JShipConfigField;
import com.github.manolo8.darkbot.gui.tree.components.JStringField;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import eu.darkbot.api.config.ConfigSetting;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class EditorManager {

    // Standard editors for specific data types
    private final Map<Class<?>, OptionEditor> editorsByType = new HashMap<>();
    // Editors shared between diff TreeEditor instances
    private final Map<Class<? extends OptionEditor>, OptionEditor> sharedEditors;
    // Editors for specific @Editor annotations in fields
    private final Map<Class<? extends OptionEditor>, OptionEditor> editorsByClass = new HashMap<>();
    private final OptionEditor defaultEditor = new JLabelField();

    public EditorManager() {
        this(null);
    }

    public EditorManager(EditorManager shared) {
        this.sharedEditors = shared != null ? shared.sharedEditors : new HashMap<>();
        this.defaultEditor.getComponent().setOpaque(false);

        addEditor(new JCharField.ExtraBorder(), Character.class);
        addEditor(new JBoolField(), boolean.class);
        addEditor(new JNumberField(), double.class, int.class);
        addEditor(new JStringField(), String.class);
        addEditor(new JShipConfigField(), Config.ShipConfig.class);
        addEditor(new JRangeField(), Config.PercentRange.class);
        addEditor(new JPlayerTagField(), PlayerTag.class);
        addEditor(new JColorField(), Color.class);
        addEditor(new JFontField(), Font.class);
        addEditor(new JConditionField(), Condition.class);
    }

    private void addEditor(OptionEditor editor, Class<?>... types) {
        for (Class<?> type : types) this.editorsByType.put(type, editor);
        editor.getComponent().setOpaque(false);
    }

    public OptionEditor getEditor(ConfigField field) {
        Class<? extends OptionEditor> editorClass = field.getEditor();
        if (field.getEditor() == null) return editorsByType.getOrDefault(field.field.getType(), defaultEditor);
        Map<Class<? extends OptionEditor>, OptionEditor> editorMap = field.isSharedEditor() ? sharedEditors : editorsByClass;
        return editorMap.computeIfAbsent(editorClass,
                c -> ReflectionUtils.createInstance(c, field.getParent().getClass(), field.getParent()));
    }

    public int getWidthFor(ConfigSetting<?> node, FontMetrics font) {
        if (node.getName().isEmpty()) return 0;
        if (node instanceof ConfigSetting.Parent) return font.stringWidth(node.getName()) + 5;

        ConfigSetting.Parent<?> parent = node.getParent();
        return (parent == null ? Stream.of(node) : parent.getChildren().values().stream())
                .filter(cs -> !(cs instanceof ConfigSetting.Parent))
                .map(ConfigSetting::getName)
                .mapToInt(font::stringWidth)
                .max()
                .orElseGet(() -> font.stringWidth(node.getName())) + 10;
    }

}
