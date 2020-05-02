package com.github.manolo8.darkbot.gui.zones;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.gui.components.TabbedPane;
import com.github.manolo8.darkbot.gui.zones.safety.SafetiesEditor;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ZonesEditor extends JPanel {

    private TabbedPane tabbedPane = new TabbedPane();
    private ZoneEditor preferredZones = new ZoneEditor();
    private ZoneEditor avoidedZones = new ZoneEditor();
    private SafetiesEditor safeEditor = new SafetiesEditor();

    public ZonesEditor() {
        super(new MigLayout("ins 0, gap 0, wrap 3", "[grow][grow][grow]", "[][grow]"));
        tabbedPane.setBorder(null);

        tabbedPane.addTab(null, "tabs.preferred_zones", preferredZones);
        tabbedPane.addTab(null, "tabs.avoided_zones", avoidedZones);
        tabbedPane.addTab(null, "tabs.safety_places", safeEditor);

        tabbedPane.getHeader().forEach(tab -> this.add(tab, "grow"));
        add(tabbedPane, "span 3, grow");
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
