package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.gui.components.MainButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class NativeCard extends GenericFeaturesCard {

    // This is static so that reloading plugins keeps it hidden.
    // It shouldn't be static but it's the easiest way for now.
    private static boolean hidden = true;

    private final Main main;
    private final FeatureRegistry featureRegistry;
    private final NativeName name;

    NativeCard(Main main, FeatureRegistry featureRegistry) {
        this.main = main;
        this.featureRegistry = featureRegistry;

        setColor(null);

        this.name = new NativeName(new ToggleVisibilityButton());
        setup();
    }

    private void setup() {
        removeAll();
        add(name, "dock north");

        if (!hidden)
            featureRegistry.getFeatures((Plugin) null)
                    .forEach(fd -> this.addFeature(main, fd));
    }

    private class NativeName extends JPanel {
        NativeName(ToggleVisibilityButton btn) {
            super(new MigLayout("", "[][]push[]"));

            JLabel name = new JLabel("Builtin features");
            Font baseFont = name.getFont();
            name.setFont(baseFont.deriveFont(baseFont.getStyle() | Font.BOLD));

            JLabel desc = new JLabel("(required internal functionality, they can't be disabled, hover for more info)");
            desc.setFont(baseFont.deriveFont(baseFont.getStyle() | Font.ITALIC));

            setToolTipText("These built-in features do not belong to any particular plugin,\n" +
                    "they are part of the bot itself and are required for the bot to work.\n" +
                    "The reason they are displayed here is so that errors on them can be seen and fixed.");

            add(name);
            add(desc);
            add(btn, "height 16!");

            setOpaque(false);
        }
    }

    class ToggleVisibilityButton extends MainButton {

        ToggleVisibilityButton() {
            super(NativeCard.hidden ? "Show" : "Hide");

            putClientProperty("JComponent.minimumWidth", 0);

            // Display as a link, blue & underlined plain-text
            setContentAreaFilled(false);
            setForeground(UIManager.getColor("Component.linkColor"));
            setFont(getFont().deriveFont(UNDERLINE));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            NativeCard.hidden = !NativeCard.hidden;
            setText(NativeCard.hidden ? "Show" : "Hide");

            setup();
        }
    }

}
