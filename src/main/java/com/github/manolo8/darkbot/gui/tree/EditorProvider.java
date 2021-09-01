package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.types.Editor;
import com.github.manolo8.darkbot.gui.tree.editors.BooleanEditor;
import com.github.manolo8.darkbot.gui.tree.editors.ColorEditor;
import com.github.manolo8.darkbot.gui.tree.editors.ConditionEditor;
import com.github.manolo8.darkbot.gui.tree.editors.FontEditor;
import com.github.manolo8.darkbot.gui.tree.editors.NumberEditor;
import com.github.manolo8.darkbot.gui.tree.editors.PercentEditor;
import com.github.manolo8.darkbot.gui.tree.editors.RangeEditor;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.types.PercentRange;
import eu.darkbot.api.config.util.OptionEditor;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.utils.Inject;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class EditorProvider {

    private final PluginAPI api;
    private final LegacyEditorManager legacy;

    // object type class -> option editor class
    private final Map<Class<?>, Class<? extends OptionEditor>> defaultEditors = new HashMap<>();

    // metadata key -> option editor class
    private final Map<String, Class<? extends OptionEditor>> metadataEditors = new LinkedHashMap<>();

    // option editor class -> option editor instance
    private final Map<Class<? extends OptionEditor>, OptionEditor<?>> instances = new HashMap<>();


    @Inject
    public EditorProvider(PluginAPI api,
                          LegacyEditorManager legacy) {
        this.api = api;
        this.legacy = legacy;

        defaultEditors.put(Boolean.class, BooleanEditor.class);
        defaultEditors.put(Integer.class, NumberEditor.class);
        defaultEditors.put(Double.class, NumberEditor.class);
        defaultEditors.put(Condition.class, ConditionEditor.class);
        defaultEditors.put(Color.class, ColorEditor.class);
        defaultEditors.put(Font.class, FontEditor.class);

        defaultEditors.put(Config.PercentRange.class, RangeEditor.class);
        defaultEditors.put(PercentRange.class, RangeEditor.class);

        metadataEditors.put("isPercent", PercentEditor.class);
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public EditorProvider(EditorProvider shared) {
        this(shared.api, new LegacyEditorManager(shared.legacy));
    }

    public <T> OptionEditor<T> getEditor(ConfigSetting<T> setting) {
        // Legacy handler should take care of this field
        Field field = setting.getHandler().getMetadata("field");
        if (field != null && field.isAnnotationPresent(Editor.class)) return null;

        // Specific editor class requested
        Class<? extends OptionEditor> editor = setting.getHandler().getMetadata("editor");

        // Metadata based editors
        if (editor == null)
            editor = metadataEditors.entrySet().stream()
                    .filter(e -> setting.getHandler().getMetadata(e.getKey()) != null)
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);

        // Default editors
        if (editor == null) editor = defaultEditors.get(setting.getType());

        if (editor == null) return null;
        return (OptionEditor<T>) instances.computeIfAbsent(editor, api::requireInstance);
    }

    public com.github.manolo8.darkbot.gui.tree.OptionEditor getLegacyEditor(ConfigField field) {
        return legacy.getEditor(field);
    }



}
