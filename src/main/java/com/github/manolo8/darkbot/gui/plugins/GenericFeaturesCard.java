package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.plugins.PluginIssue;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import eu.darkbot.api.extensions.Configurable;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.Collections;
import java.util.Map;

public abstract class GenericFeaturesCard extends JPanel {

    private static final Border LOADED_BORDER = BorderFactory.createLineBorder(UIUtils.GREEN),
            WARNING_BORDER = BorderFactory.createLineBorder(UIUtils.YELLOW),
            ERROR_BORDER = BorderFactory.createLineBorder(UIUtils.RED);
    private static final int ALPHA = 48 << 24;
    private static final Color LOADED_COLOR = new Color(UIUtils.GREEN.getRGB() + ALPHA, true),
            WARNING_COLOR = new Color(UIUtils.YELLOW.getRGB() + ALPHA, true),
            ERROR_COLOR = new Color(UIUtils.RED.getRGB() + ALPHA, true);

    protected static final Map<TextAttribute, Object> UNDERLINE =
            Collections.singletonMap(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);

    GenericFeaturesCard() {
        super(new MigLayout("fillx, gapy 0, ins 0 0 5px 0", "5px[]0px[]10px[]10px[grow]", "[]"));
    }

    protected void addFeature(Main main, FeatureDefinition<?> feature) {
        add(new FeatureTypeButton(feature), "growx");
        if (Configurable.class.isAssignableFrom(feature.getClazz())) {
            //noinspection unchecked
            add(new FeatureConfigButton(main, (FeatureDefinition<Configurable<?>>) feature), "growy");
        } else {
            add(new JLabel());
        }
        add(new FeatureCheckbox(feature));
        add(new IssueList(true, feature.getIssues()), "hidemode 2, wrap");
    }

    protected void setColor(PluginIssue.Level level) {
        if (level == PluginIssue.Level.ERROR) {
            setBorder(ERROR_BORDER);
            setBackground(ERROR_COLOR);
        } else if (level == PluginIssue.Level.WARNING) {
            setBorder(WARNING_BORDER);
            setBackground(WARNING_COLOR);
        } else {
            setBorder(LOADED_BORDER);
            setBackground(LOADED_COLOR);
        }
        setOpaque(false);
    }

    @Override
    public void paintComponent(Graphics g) {
        // Panels don't render a background if set to opaque = false
        // But opaque = false is required since the background is not completely opaque.
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

}
