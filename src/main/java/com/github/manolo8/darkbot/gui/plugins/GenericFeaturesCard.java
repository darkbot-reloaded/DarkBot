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
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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

    private final List<FeatureRow> featureRows = new ArrayList<>();

    GenericFeaturesCard() {
        super(new MigLayout("fillx, gapy 0, ins 0 0 5px 0", "5px[]0px[]10px[]10px[grow]", "[]"));
    }

    protected void addFeature(Main main, FeatureDefinition<?> feature) {
        FeatureTypeButton typeButton = new FeatureTypeButton(feature);
        FeatureCheckbox checkbox = new FeatureCheckbox(feature);

        add(typeButton, "growx");
        if (Configurable.class.isAssignableFrom(feature.getClazz())) {
            //noinspection unchecked
            add(new FeatureConfigButton(main, (FeatureDefinition<Configurable<?>>) feature), "growy");
        } else {
            add(new JLabel());
        }
        add(checkbox);
        add(new IssueList(true, feature.getIssues()), "hidemode 2, wrap");

        featureRows.add(new FeatureRow(feature.getName(), typeButton, checkbox));
    }

    @Override
    public void removeAll() {
        super.removeAll();
        featureRows.clear();
    }

    /**
     * Filters the features of this card by name. If the card name matches, every feature is shown as normal,
     * otherwise features that don't match the query are dimmed.
     *
     * @param cardName name of the card, matched against the query
     * @param query    normalized query, as returned by {@link #normalizeSearch(String)}
     * @return true if the card name or any of its features matches the query
     */
    protected boolean filterFeatures(String cardName, String query) {
        boolean cardMatches = query.isEmpty() || normalizeSearch(cardName).contains(query);
        boolean anyFeatureMatches = false;
        for (FeatureRow row : featureRows) {
            boolean featureMatches = row.matches(query);
            anyFeatureMatches |= featureMatches;
            row.setDimmed(!cardMatches && !featureMatches);
        }
        return cardMatches || anyFeatureMatches;
    }

    /**
     * Normalizes text for searching: lower-cased, accents removed and whitespace collapsed.
     */
    static String normalizeSearch(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
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

    private static class FeatureRow {
        private final String name;
        private final JComponent[] components;
        private final Color[] foregrounds;
        private boolean dimmed;

        private FeatureRow(String name, JComponent... components) {
            this.name = normalizeSearch(name);
            this.components = components;
            this.foregrounds = new Color[components.length];
        }

        private boolean matches(String query) {
            return query.isEmpty() || name.contains(query);
        }

        private void setDimmed(boolean dimmed) {
            if (this.dimmed == dimmed) return;
            this.dimmed = dimmed;

            Color dimColor = UIManager.getColor("Label.disabledForeground");
            for (int i = 0; i < components.length; i++) {
                if (dimmed) {
                    foregrounds[i] = components[i].getForeground();
                    if (dimColor != null) components[i].setForeground(dimColor);
                } else {
                    components[i].setForeground(foregrounds[i]);
                }
            }
        }
    }

}
