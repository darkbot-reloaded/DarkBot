package com.github.manolo8.darkbot.gui.zones;

import com.formdev.flatlaf.FlatClientProperties;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.gui.zones.safety.SafetiesEditor;
import com.github.manolo8.darkbot.utils.I18n;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ZonesEditor extends JTabbedPane {
    private final ZoneEditor preferredZones = new ZoneEditor();
    private final ZoneEditor avoidedZones = new ZoneEditor();
    private final SafetiesEditor safeEditor = new SafetiesEditor();

    public ZonesEditor() {
        putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_FILL);
        putClientProperty(FlatClientProperties.TABBED_PANE_TAB_HEIGHT, 25);

        addTab("tabs.preferred_zones", preferredZones);
        addTab("tabs.avoided_zones", avoidedZones);
        addTab("tabs.safety_places", safeEditor);
    }

    @Override
    public void addTab(@NotNull String key, Component component) {
        String title = I18n.getOrDefault(key, null);
        String description = I18n.getOrDefault(key + ".desc", null);

        super.addTab(title, null, component, description);
    }

    public void setup(Main main) {
        // Create zones for current map if missing
        ConfigEntity.INSTANCE.getOrCreatePreferred();
        ConfigEntity.INSTANCE.getOrCreateAvoided();
        ConfigEntity.INSTANCE.getOrCreateSafeties();

        preferredZones.setup(main, main.config.PREFERRED);
        avoidedZones.setup(main, main.config.AVOIDED);
        safeEditor.setup(main);
    }

}
