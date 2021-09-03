package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.tree.components.JCharField;
import com.github.manolo8.darkbot.gui.tree.components.JLabelField;
import com.github.manolo8.darkbot.gui.tree.components.JShipConfigField;
import com.github.manolo8.darkbot.gui.tree.components.JStringField;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import eu.darkbot.api.config.util.ValueHandler;
import eu.darkbot.api.utils.Inject;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class LegacyEditorManager {

    // Standard editors for specific data types
    private final Map<Class<?>, OptionEditor> editorsByType = new HashMap<>();
    // Editors shared between diff TreeEditor instances
    private final Map<Class<? extends OptionEditor>, OptionEditor> sharedEditors;
    // Editors for specific @Editor annotations in fields
    private final Map<Class<? extends OptionEditor>, OptionEditor> editorsByClass = new HashMap<>();
    private final OptionEditor defaultEditor = new JLabelField();


    private final Map<Class<? extends ValueHandler<?>>, eu.darkbot.api.config.util.OptionEditor<?>> newEditors = new HashMap<>();

    @Inject
    public LegacyEditorManager() {
        this(null);
    }

    public LegacyEditorManager(LegacyEditorManager shared) {
        this.sharedEditors = shared != null ? shared.sharedEditors : new HashMap<>();
        this.defaultEditor.getComponent().setOpaque(false);

        addEditor(new JCharField.ExtraBorder(), Character.class);
        addEditor(new JStringField(), String.class);
        addEditor(new JShipConfigField(), Config.ShipConfig.class);
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

}
