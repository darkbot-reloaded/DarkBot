package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.PlayerTag;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.tree.ConfigNode;
import com.github.manolo8.darkbot.gui.tree.components.JBoolField;
import com.github.manolo8.darkbot.gui.tree.components.JCharField;
import com.github.manolo8.darkbot.gui.tree.components.JLabelField;
import com.github.manolo8.darkbot.gui.tree.components.JNumberField;
import com.github.manolo8.darkbot.gui.tree.components.JPlayerTagField;
import com.github.manolo8.darkbot.gui.tree.components.JRangeField;
import com.github.manolo8.darkbot.gui.tree.components.JShipConfigField;
import com.github.manolo8.darkbot.gui.tree.components.JStringField;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class EditorManager {

    // Standard editors for specific data types
    private Map<Class, OptionEditor> editorsByType = new HashMap<>();
    // Editors shared between diff TreeEditor instances
    private Map<Class<? extends OptionEditor>, OptionEditor> sharedEditors = new HashMap<>();
    // Editors for specific @Editor annotations in fields
    private Map<Class<? extends OptionEditor>, OptionEditor> editorsByClass = new HashMap<>();
    private OptionEditor defaultEditor = new JLabelField();

    public EditorManager() {
        this(null);
    }

    public EditorManager(EditorManager shared) {
        if (shared != null) this.sharedEditors = shared.sharedEditors;
        defaultEditor.getComponent().setOpaque(false);

        addEditor(new JCharField(), Character.class);
        addEditor(new JBoolField(), boolean.class);
        addEditor(new JNumberField(), double.class, int.class);
        addEditor(new JStringField(), String.class);
        addEditor(new JShipConfigField(), Config.ShipConfig.class);
        addEditor(new JRangeField(), Config.PercentRange.class);
        addEditor(new JPlayerTagField(), PlayerTag.class);
    }

    private void addEditor(OptionEditor editor, Class... types) {
        for (Class type : types) this.editorsByType.put(type, editor);
        editor.getComponent().setOpaque(false);
    }

    public OptionEditor getEditor(ConfigField field) {
        Class<? extends OptionEditor> editorClass = field.getEditor();
        if (field.getEditor() == null) return editorsByType.getOrDefault(field.field.getType(), defaultEditor);
        Map<Class<? extends OptionEditor>, OptionEditor> editorMap = field.isSharedEditor() ? sharedEditors : editorsByClass;
        return editorMap.computeIfAbsent(editorClass,
                c -> ReflectionUtils.createInstance(c, (Class<Object>) field.parent.getClass(), field.parent));
    }

    public int getWidthFor(ConfigNode node, FontMetrics font) {
        if (I18n.getOrDefault(node.key, node.name).isEmpty()) return 0;
        return node instanceof ConfigNode.Leaf ?
                font.stringWidth(node.getLongestSibling()) + 10 :
                font.stringWidth(I18n.getOrDefault(node.key, node.name)) + 5;
    }

}
