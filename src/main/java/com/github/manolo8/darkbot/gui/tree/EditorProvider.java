package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.PlayerTag;
import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.gui.tree.components.JBoolField;
import com.github.manolo8.darkbot.gui.tree.components.JCharField;
import com.github.manolo8.darkbot.gui.tree.components.JColorField;
import com.github.manolo8.darkbot.gui.tree.components.JConditionField;
import com.github.manolo8.darkbot.gui.tree.components.JFontField;
import com.github.manolo8.darkbot.gui.tree.components.JNumberField;
import com.github.manolo8.darkbot.gui.tree.components.JPlayerTagField;
import com.github.manolo8.darkbot.gui.tree.components.JRangeField;
import com.github.manolo8.darkbot.gui.tree.components.JShipConfigField;
import com.github.manolo8.darkbot.gui.tree.components.JStringField;
import eu.darkbot.api.API;
import eu.darkbot.api.events.EventHandler;
import eu.darkbot.api.events.Listener;
import eu.darkbot.api.managers.ExtensionsAPI;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class EditorProvider implements API.Singleton, Listener {

    private final Map<Class<?>, Class<? extends OptionEditor>> editors = new HashMap<>();

    public EditorProvider() {
        setupDefaults();
    }

    private void setupDefaults() {
        addEditor(JCharField.ExtraBorder.class, Character.class);
        addEditor(JBoolField.class, boolean.class);
        addEditor(JNumberField.class, double.class, int.class);
        addEditor(JStringField.class, String.class);
        addEditor(JShipConfigField.class, Config.ShipConfig.class);
        addEditor(JRangeField.class, Config.PercentRange.class);
        addEditor(JPlayerTagField.class, PlayerTag.class);
        addEditor(JColorField.class, Color.class);
        addEditor(JFontField.class, Font.class);
        addEditor(JConditionField.class, Condition.class);
    }

    private void addEditor(Class<? extends OptionEditor> editor, Class<?>... types) {
        for (Class<?> type : types)
            this.editors.put(type, editor);
    }

    public boolean hasEditor(Class<?> cls) {
        return editors.containsKey(cls);
    }

    @EventHandler
    public void onPluginChange(ExtensionsAPI.PluginLifetimeEvent event) {
        if (event.getStage() == ExtensionsAPI.PluginStage.BEFORE_LOAD) {
            editors.clear();
            setupDefaults();
        } else if (event.getStage() == ExtensionsAPI.PluginStage.AFTER_LOAD_COMPLETE) {
            // Register editors for plugins
        }
    }

}
